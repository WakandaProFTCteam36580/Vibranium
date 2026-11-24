package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.drive.AprilTagWebCam;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

@Autonomous(name = "Autonomo Principal BioBuzz", group = "Competition")
public class AutonomoBioBuzz extends LinearOpMode {

    // =====================================================================
    // ROTINA: mesmos números do MeepMeep (polegadas / graus, aliança vermelha).
    // Heading 0 = apontando para +x, positivo = anti-horário (igual ao MeepMeep e ao IMU).
    // =====================================================================
    private static final double START_X = -18, START_Y = -38.5, START_DEG = 72; // de frente p/ colmeia de baixo
    private static final double WALL_DIST = 8;                    // ida reta até o pólen da parede
    private static final double LANE_X = -27;                     // faixa junto à parede esquerda
    private static final double LANE_TOP_Y = 42;
    private static final double FLOWER_X = -12.6, FLOWER_Y = 42, FLOWER_DEG = 90;
    private static final double HIVE_DEG_AT_FLOWER = -79;         // colmeia de cima vista da flower
    private static final double PARK_X = -26, PARK_Y = 28, PARK_DEG = 90;

    // Lado 2 (tags 34-37): true = espelha a rotina (como o código antigo fazia);
    // false = gira a rotina 180 graus (se a arena for simétrica por rotação, como na imagem).
    private static final boolean LADO2_ESPELHADO = true;

    // =====================================================================
    // CALIBRAR NA PISTA
    // =====================================================================
    // Compensação angular caso o conjunto frontal esteja deslocado para a direita
    private static final double COMPENSACAO_DESLOCAMENTO_GRAUS = -3.0;
    // Tempo (ms) para andar 1 polegada com POTENCIA_RETA. Meça: ande 40 pol e divida o tempo por 40.
    private static final double MS_POR_POLEGADA = 45.0;
    private static final double POTENCIA_RETA = 0.5;
    private static final double KP_HEADING = 0.02;   // correção para andar reto usando o IMU
    private static final long COLETA_FLOWER_MS = 1500;

    // Câmara e AprilTags
    private final AprilTagWebCam tagWebCam = new AprilTagWebCam();
    private int tagConfirmada = -1;
    private double anguloTagYaw = 0.0;
    private boolean leuAnguloValido = false;

    // Motores de Tração e Lançamento/Coleta
    private DcMotor motorDir;
    private DcMotor motorEsq;
    private DcMotor motorIt;

    // Servos Contínuos de Alimentação/Coleta
    private CRServo servoitDir;
    private CRServo servoitEsq;

    // Giroscópio Interno (IMU)
    private IMU imu;

    // Navegação estimada (mesmo sistema de coordenadas do MeepMeep, já ajustado ao lado)
    private boolean lado2 = false;
    private double poseX, poseY;
    private double refDeg = 0.0;          // heading de campo correspondente ao yaw 0 do IMU
    private double headingAlvoYaw = 0.0;  // yaw que o robô deve manter ao andar

    // Temporizador
    private final ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        inicializarHardware();
        tagWebCam.init(hardwareMap, telemetry);

        telemetry.addData("Status", "Pronto! Robô de frente para a colmeia...");
        telemetry.update();

        // Leitura contínua das AprilTags (30-37) durante o INIT
        while (!isStarted() && !isStopRequested()) {
            localizarAprilTags();

            if (tagConfirmada != -1) {
                String lado = (tagConfirmada <= 33) ? "LADO 1 (Tags 30-33)" : "LADO 2 (Tags 34-37)";
                telemetry.addData("Identificação", lado + " - Tag " + tagConfirmada);
                telemetry.addData("Ângulo Tag (Yaw)", "%.1f graus", anguloTagYaw);
            } else {
                telemetry.addData("Identificação", "Nenhuma tag (30-37) detetada ainda");
            }
            telemetry.addData("Ângulo IMU Atual", "%.1f graus", getAnguloAtual());
            telemetry.update();
        }

        waitForStart();

        if (opModeIsActive()) {
            imu.resetYaw();
            localizarAprilTags();

            lado2 = (tagConfirmada >= 34 && tagConfirmada <= 37);
            poseX = tx(START_X);
            poseY = ty(START_Y);

            // 1. De frente para a colmeia: alinha e lança
            alinharDeFrenteParaColmeia();
            tagWebCam.stop();
            lancarElementos();

            // 2. Vai reto até a parede, pega o pólen do canto, volta e lança de novo
            pegarPolenDaParede();
            lancarElementos();

            // 3. Sobe pela faixa da parede até a flower
            irParaFlower();

            // 4. Alinha com a colmeia e lança
            mirarColmeia(HIVE_DEG_AT_FLOWER);
            lancarElementos();

            // 5. Vira para a flower, pega os polens, vira para a colmeia e lança
            coletarNaFlower();
            mirarColmeia(HIVE_DEG_AT_FLOWER);
            lancarElementos();

            // 6. Estaciona
            irParaEstacionamento();
        }
    }

    private void inicializarHardware() {
        motorDir = hardwareMap.get(DcMotor.class, "motor_dir");
        motorEsq = hardwareMap.get(DcMotor.class, "motor_esq");
        motorIt = hardwareMap.get(DcMotor.class, "motor_it");

        servoitDir = hardwareMap.get(CRServo.class, "servoit_dir");
        servoitEsq = hardwareMap.get(CRServo.class, "servoit_esq");

        motorDir.setDirection(DcMotorSimple.Direction.REVERSE);
        motorEsq.setDirection(DcMotorSimple.Direction.FORWARD);
        motorIt.setDirection(DcMotorSimple.Direction.FORWARD);

        motorDir.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorEsq.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorIt.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        imu = hardwareMap.get(IMU.class, "imu");
        IMU.Parameters parametrosImu = new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
                )
        );
        imu.initialize(parametrosImu);
        imu.resetYaw();
    }

    private void localizarAprilTags() {
        tagWebCam.update();
        AprilTagSingleDetection deteccao = tagWebCam.getFirstDetectedTag();

        if (deteccao != null) {
            int id = deteccao.id;
            if (id >= 30 && id <= 37) {
                tagConfirmada = id;
                if (deteccao.ftcPose != null) {
                    anguloTagYaw = deteccao.ftcPose.yaw;
                    leuAnguloValido = true;
                }
            }
        }
    }

    // =====================================================================
    // Etapas da rotina
    // =====================================================================

    private void alinharDeFrenteParaColmeia() {
        fase("1 - Alinhando de frente para a colmeia");

        if (leuAnguloValido && Math.abs(anguloTagYaw) > 1.5) {
            girarPorAngulo(-anguloTagYaw + COMPENSACAO_DESLOCAMENTO_GRAUS);
        } else {
            girarPorAngulo(COMPENSACAO_DESLOCAMENTO_GRAUS);
        }
        sleep(150);

        // A partir daqui, yaw 0 = robô apontando para a colmeia (heading START_DEG da rotina)
        imu.resetYaw();
        refDeg = th(START_DEG) + COMPENSACAO_DESLOCAMENTO_GRAUS;
        headingAlvoYaw = 0.0;
    }

    private void pegarPolenDaParede() {
        fase("2 - Ida reta até a parede para pegar o pólen");

        girarParaCampo(th(180));          // de frente para a parede esquerda
        ativarColeta(true);
        andar(WALL_DIST);
        ativarColeta(false);
        andar(-WALL_DIST);
        mirarColmeia(START_DEG);          // volta a mirar a colmeia
    }

    private void irParaFlower() {
        fase("3 - Subindo pela faixa da parede até a flower");

        irPara(LANE_X, START_Y);          // para a esquerda
        irPara(LANE_X, LANE_TOP_Y);       // sobe
        irPara(FLOWER_X, FLOWER_Y);       // até a flower
    }

    private void coletarNaFlower() {
        fase("5 - Virando para a flower e pegando os polens");

        girarParaCampo(th(FLOWER_DEG));
        ativarColeta(true);
        sleep(COLETA_FLOWER_MS);
        ativarColeta(false);
    }

    private void irParaEstacionamento() {
        fase("6 - Indo para o estacionamento");

        irPara(PARK_X, PARK_Y);
        girarParaCampo(th(PARK_DEG));

        pararMotores();
        telemetry.addData("Status", "Autônomo concluído!");
        telemetry.update();
    }

    private void lancarElementos() {
        telemetry.addData("Ação", "Lançando elementos");
        telemetry.update();

        motorIt.setPower(0.8);
        sleep(450);

        servoitDir.setPower(1.0);
        servoitEsq.setPower(1.0);
        sleep(1600);

        servoitDir.setPower(0.0);
        servoitEsq.setPower(0.0);
        motorIt.setPower(0.0);
    }

    private void ativarColeta(boolean ligar) {
        double potenciaServo = ligar ? -0.8 : 0.0; // Ajuste o sinal conforme o sentido de recolher
        servoitDir.setPower(potenciaServo);
        servoitEsq.setPower(potenciaServo);
    }

    private void fase(String texto) {
        telemetry.addData("Fase", texto);
        telemetry.update();
    }

    // =====================================================================
    // Transformação para o lado 2 (a rotina é escrita para o lado 1)
    // =====================================================================

    private double tx(double x) {
        return lado2 ? -x : x;
    }

    private double ty(double y) {
        return (lado2 && !LADO2_ESPELHADO) ? -y : y;
    }

    private double th(double deg) {
        if (!lado2) return deg;
        return LADO2_ESPELHADO ? 180.0 - deg : deg + 180.0;
    }

    // =====================================================================
    // Navegação
    // =====================================================================

    // Vai em linha reta até um ponto (coordenadas do lado 1): gira apontando para ele e anda
    private void irPara(double xLado1, double yLado1) {
        double dx = tx(xLado1) - poseX;
        double dy = ty(yLado1) - poseY;
        double dist = Math.hypot(dx, dy);
        if (dist < 0.5) return;

        girarParaCampo(Math.toDegrees(Math.atan2(dy, dx)));
        andar(dist);
    }

    // Aponta para a colmeia (heading do lado 1) já com a compensação do conjunto frontal
    private void mirarColmeia(double degLado1) {
        girarParaCampo(th(degLado1) + COMPENSACAO_DESLOCAMENTO_GRAUS);
    }

    // Gira até um heading de campo (mesmo sistema do MeepMeep, já transformado para o lado)
    private void girarParaCampo(double degCampo) {
        girarParaYaw(normalizarAngulo(degCampo - refDeg));
    }

    // Gira até um yaw absoluto do IMU
    private void girarParaYaw(double alvoYaw) {
        headingAlvoYaw = alvoYaw;
        double erro = normalizarAngulo(alvoYaw - getAnguloAtual());

        runtime.reset();
        while (opModeIsActive() && Math.abs(erro) > 1.5 && runtime.seconds() < 3.0) {
            erro = normalizarAngulo(alvoYaw - getAnguloAtual());

            double potencia = Math.max(0.22, Math.min(0.55, Math.abs(erro) * 0.015));

            if (erro > 0) {
                motorDir.setPower(potencia);
                motorEsq.setPower(-potencia);
            } else {
                motorDir.setPower(-potencia);
                motorEsq.setPower(potencia);
            }
        }
        pararMotores();
        sleep(100);
    }

    // Anda em linha reta (polegadas; negativo = ré) segurando o heading com o IMU
    private void andar(double polegadas) {
        if (polegadas == 0) return;

        double sinal = Math.signum(polegadas);
        long tempoMs = (long) (Math.abs(polegadas) * MS_POR_POLEGADA);

        runtime.reset();
        while (opModeIsActive() && runtime.milliseconds() < tempoMs) {
            double erro = normalizarAngulo(headingAlvoYaw - getAnguloAtual());
            double correcao = Math.max(-0.15, Math.min(0.15, erro * KP_HEADING));
            double potencia = sinal * POTENCIA_RETA;

            // correcao > 0 => corrige para a esquerda (anti-horário), também em ré
            motorDir.setPower(potencia + correcao);
            motorEsq.setPower(potencia - correcao);
        }

        // Atualiza a posição estimada
        double rad = Math.toRadians(refDeg + headingAlvoYaw);
        poseX += polegadas * Math.cos(rad);
        poseY += polegadas * Math.sin(rad);

        pararMotoresTracao();
        sleep(100);
    }

    // Gira por um ângulo relativo (usado só no alinhamento inicial com a AprilTag)
    private void girarPorAngulo(double grausAlvo) {
        double anguloInicial = getAnguloAtual();
        double erro = grausAlvo;

        runtime.reset();
        while (opModeIsActive() && Math.abs(erro) > 1.5 && runtime.seconds() < 2.5) {
            double delta = normalizarAngulo(getAnguloAtual() - anguloInicial);
            erro = grausAlvo - delta;

            double potencia = Math.max(0.22, Math.min(0.55, Math.abs(erro) * 0.015));

            if (erro > 0) {
                motorDir.setPower(potencia);
                motorEsq.setPower(-potencia);
            } else {
                motorDir.setPower(-potencia);
                motorEsq.setPower(potencia);
            }
        }
        pararMotores();
    }

    // Para só a tração (mantém a coleta ligada, se estiver)
    private void pararMotoresTracao() {
        motorDir.setPower(0.0);
        motorEsq.setPower(0.0);
    }

    private void pararMotores() {
        motorDir.setPower(0.0);
        motorEsq.setPower(0.0);
        motorIt.setPower(0.0);
        servoitDir.setPower(0.0);
        servoitEsq.setPower(0.0);
    }

    private double getAnguloAtual() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    private double normalizarAngulo(double angulo) {
        while (angulo > 180) angulo -= 360;
        while (angulo < -180) angulo += 360;
        return angulo;
    }
}