package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.drive.AprilTagWebCam;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

@Autonomous(name = "Autonomo BioBuzz - Parte Inicial", group = "Competition")
public class Teste_Auto extends LinearOpMode {

    // =====================================================================
    // AJUSTES RÁPIDOS DE CALIBRAÇÃO
    // =====================================================================
    // Mantenha o sinal que você já corrigiu na pista para o alinhamento da tag
    private static final double INVERTER_YAW_TAG = -1.0;

    // Compensação do deslocamento lateral do conjunto
    private static final double COMPENSACAO_DESLOCAMENTO_GRAUS = -3.0;

    // Giro principal para virar a frente (intake) em direção aos pólens após o lançamento
    private static final double GIRO_PARA_COLETA_GRAUS = -90.0;

    // Distância maior para andar para frente por mais tempo capturando com o intake
    private static final double DISTANCIA_COLETA_POLENS = 40.0;

    // Giro adicional para "encostar" melhor a frente/intake nos pólens ou na faixa de coleta
    private static final double AJUSTE_GIRO_ENCOSTAR_GRAUS = 15.0;
    private static final double DISTANCIA_ENCOSTAR_FINAL = 6.0;

    private static final double MS_POR_POLEGADA = 45.0;
    private static final double POTENCIA_RETA = 0.5;
    private static final double KP_HEADING = 0.02;

    // Shooter / lift / intake (mesmos parâmetros do TeleOp)
    private static final double POT_SHOOTER = 0.63;
    private static final double POT_SHOOTER_FREIO = -0.07;
    private static final long SHOOTER_SPINUP_MS = 1000;
    private static final long SHOOTER_FREIO_MS = 200;
    private static final double POT_LIFT = 1.0;
    private static final long LIFT_MS = 2000;
    private static final double POT_INTAKE = 1.0;

    // Câmera e AprilTags
    private final AprilTagWebCam tagWebCam = new AprilTagWebCam();
    private int tagConfirmada = -1;
    private double anguloTagYaw = 0.0;
    private boolean leuAnguloValido = false;

    // Motores e Servos
    private DcMotor motorDir;
    private DcMotor motorEsq;
    private DcMotor motorIt;
    private DcMotor motorSh;

    private CRServo servoLift;
    private Servo servoCatraca;
    private Servo servoFlw;

    private IMU imu;
    private double headingAlvoYaw = 0.0;

    private final ElapsedTime timerShooter = new ElapsedTime();
    private final ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        inicializarHardware();
        tagWebCam.init(hardwareMap, telemetry);

        telemetry.addData("Status", "Aguardando PLAY e leitura da AprilTag (30-37)...");
        telemetry.update();

        while (!isStarted() && !isStopRequested()) {
            localizarAprilTags();

            if (tagConfirmada != -1) {
                String lado = (tagConfirmada <= 33) ? "LADO 1 (Tags 30-33)" : "LADO 2 (Tags 34-37)";
                telemetry.addData("Tag", lado + " - ID " + tagConfirmada);
                telemetry.addData("Yaw Tag", "%.1f deg", anguloTagYaw);
            } else {
                telemetry.addData("Tag", "Nenhuma tag (30-37) detectada");
            }
            telemetry.addData("Yaw IMU", "%.1f deg", getAnguloAtual());
            telemetry.update();
        }

        waitForStart();

        if (opModeIsActive()) {
            imu.resetYaw();
            localizarAprilTags();

            // 1. Liga shooter e corrige alinhamento com base na AprilTag
            ligarShooter();
            alinharComAprilTag();
            tagWebCam.stop();

            // 2. Efetua o lançamento
            lancarElementos();

            // 3. Vira, avança mais tempo para capturar e faz o giro para ficar mais encostado
            virarECapturarPolens();

            pararTudo();
            telemetry.addData("Status", "Parte inicial concluída!");
            telemetry.update();
        }
    }

    private void inicializarHardware() {
        motorDir = hardwareMap.get(DcMotor.class, "motor_dir");
        motorEsq = hardwareMap.get(DcMotor.class, "motor_esq");
        motorIt = hardwareMap.get(DcMotor.class, "motor_it");
        motorSh = hardwareMap.get(DcMotor.class, "motor_sh");

        servoLift = hardwareMap.get(CRServo.class, "servo_lift");
        servoCatraca = hardwareMap.get(Servo.class, "servo_catraca");
        servoFlw = hardwareMap.get(Servo.class, "servo_flw");

        motorDir.setDirection(DcMotorSimple.Direction.REVERSE);
        motorEsq.setDirection(DcMotorSimple.Direction.FORWARD);
        motorIt.setDirection(DcMotorSimple.Direction.REVERSE);
        motorSh.setDirection(DcMotorSimple.Direction.REVERSE);

        motorDir.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorEsq.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorIt.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        if (servoLift instanceof CRServoImplEx) {
            ((CRServoImplEx) servoLift).setPwmRange(new PwmControl.PwmRange(500, 2500));
        }
        if (servoCatraca instanceof ServoImplEx) {
            ((ServoImplEx) servoCatraca).setPwmRange(new PwmControl.PwmRange(500, 2500));
        }
        if (servoFlw instanceof ServoImplEx) {
            ((ServoImplEx) servoFlw).setPwmRange(new PwmControl.PwmRange(500, 2500));
        }
        servoLift.setDirection(CRServo.Direction.REVERSE);
        servoCatraca.setDirection(Servo.Direction.REVERSE);

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

        if (deteccao != null && deteccao.id >= 30 && deteccao.id <= 37) {
            tagConfirmada = deteccao.id;
            if (deteccao.ftcPose != null) {
                anguloTagYaw = deteccao.ftcPose.yaw;
                leuAnguloValido = true;
            }
        }
    }

    private void alinharComAprilTag() {
        telemetry.addData("Etapa", "1 - Corrigindo alinhamento pela AprilTag");
        telemetry.update();

        double ajuste = COMPENSACAO_DESLOCAMENTO_GRAUS;
        if (leuAnguloValido && Math.abs(anguloTagYaw) > 1.5) {
            ajuste += (INVERTER_YAW_TAG * anguloTagYaw);
        }

        girarPorAngulo(ajuste);
        sleep(150);

        imu.resetYaw();
        headingAlvoYaw = 0.0;
    }

    private void ligarShooter() {
        motorSh.setPower(POT_SHOOTER);
        timerShooter.reset();
    }

    private void lancarElementos() {
        telemetry.addData("Etapa", "2 - Efetuando lançamento");
        telemetry.update();

        while (opModeIsActive() && timerShooter.milliseconds() < SHOOTER_SPINUP_MS) {
            idle();
        }

        servoLift.setPower(POT_LIFT);
        sleep(LIFT_MS);
        servoLift.setPower(0.0);

        motorSh.setPower(POT_SHOOTER_FREIO);
        sleep(SHOOTER_FREIO_MS);
        motorSh.setPower(0.0);
    }

    private void virarECapturarPolens() {
        telemetry.addData("Etapa", "3 - Virando, avançando e encostando para capturar");
        telemetry.update();

        double sentidoLado = (tagConfirmada >= 34 && tagConfirmada <= 37) ? -1.0 : 1.0;

        // 1. Giro principal para apontar a frente (intake) na direção da captura
        double giroPrincipal = sentidoLado * GIRO_PARA_COLETA_GRAUS;
        girarPorAngulo(giroPrincipal);
        headingAlvoYaw = normalizarAngulo(headingAlvoYaw + giroPrincipal);
        sleep(120);

        // 2. Avanço maior para frente com o intake ligado
        motorIt.setPower(POT_INTAKE);
        andar(DISTANCIA_COLETA_POLENS);

        // 3. Giro adicional para deixar o robô mais encostado + leve avanço final de coleta
        double giroEncostar = sentidoLado * AJUSTE_GIRO_ENCOSTAR_GRAUS;
        girarPorAngulo(giroEncostar);
        headingAlvoYaw = normalizarAngulo(headingAlvoYaw + giroEncostar);
        sleep(100);

        andar(DISTANCIA_ENCOSTAR_FINAL);
        motorIt.setPower(0.0);
    }

    private void andar(double polegadas) {
        if (polegadas == 0) return;

        double sinal = Math.signum(polegadas);
        long tempoMs = (long) (Math.abs(polegadas) * MS_POR_POLEGADA);

        runtime.reset();
        while (opModeIsActive() && runtime.milliseconds() < tempoMs) {
            double erro = normalizarAngulo(headingAlvoYaw - getAnguloAtual());
            double correcao = Math.max(-0.15, Math.min(0.15, erro * KP_HEADING));
            double potencia = sinal * POTENCIA_RETA;

            motorDir.setPower(potencia + correcao);
            motorEsq.setPower(potencia - correcao);
        }
        pararTracao();
    }

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
        pararTracao();
    }

    private void pararTracao() {
        motorDir.setPower(0.0);
        motorEsq.setPower(0.0);
    }

    private void pararTudo() {
        pararTracao();
        motorIt.setPower(0.0);
        motorSh.setPower(0.0);
        servoLift.setPower(0.0);
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