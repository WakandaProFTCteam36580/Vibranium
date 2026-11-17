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

    // Câmara e AprilTags
    private final AprilTagWebCam tagWebCam = new AprilTagWebCam();
    private int tagConfirmada = -1;
    private double anguloTagYaw = 0.0;
    private boolean leuAnguloValido = false;

    // Motores de Tração e Lançamento
    private DcMotor motorDir;
    private DcMotor motorEsq;
    private DcMotor motorIt;

    // Servos Contínuos de Alimentação
    private CRServo servoitDir;
    private CRServo servoitEsq;

    // Giroscópio Interno (IMU)
    private IMU imu;

    // Temporizador
    private final ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        // 1. Inicialização do Hardware e Câmara
        inicializarHardware();
        tagWebCam.init(hardwareMap, telemetry);

        telemetry.addData("Status", "Pronto! Aponte a câmara para a AprilTag...");
        telemetry.update();

        // 2. Loop durante o INIT (antes do PLAY) para ler a Tag continuamente
        while (!isStarted() && !isStopRequested()) {
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

            // Telemetria de confirmação pré-partida
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

        // Aguarda o comando PLAY
        waitForStart();

        if (opModeIsActive()) {
            // Zera a orientação do giroscópio no início da partida
            imu.resetYaw();

            // Passo 1: Alinhar com a Colmeia
            alinharComColmeia();

            // Desliga o stream da câmara para poupar CPU do Control Hub
            tagWebCam.stop();

            // Passo 2: Lançar os néctares
            lancarNectares();

            // Passo 3: Estacionar conforme o lado identificado
            estacionarRobo();
        }
    }

    private void inicializarHardware() {
        motorDir = hardwareMap.get(DcMotor.class, "motor_dir");
        motorEsq = hardwareMap.get(DcMotor.class, "motor_esq");
        motorIt = hardwareMap.get(DcMotor.class, "motor_it");

        servoitDir = hardwareMap.get(CRServo.class, "servoit_dir");
        servoitEsq = hardwareMap.get(CRServo.class, "servoit_esq");

        // Direção dos motores
        motorDir.setDirection(DcMotorSimple.Direction.REVERSE);
        motorEsq.setDirection(DcMotorSimple.Direction.FORWARD);
        motorIt.setDirection(DcMotorSimple.Direction.FORWARD);

        // Travagem ativa (BRAKE) para evitar deslizamento
        motorDir.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorEsq.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorIt.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Configuração do IMU
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

    private void alinharComColmeia() {
        telemetry.addData("Fase", "1 - A alinhar com a Colmeia");
        telemetry.update();

        if (leuAnguloValido && Math.abs(anguloTagYaw) > 1.5) {
            girarPorAngulo(-anguloTagYaw);
        } else {
            switch (tagConfirmada) {
                case 30:
                case 34:
                    girarPorAngulo(15.0);
                    break;
                case 31:
                case 35:
                    girarPorAngulo(5.0);
                    break;
                case 32:
                case 36:
                    girarPorAngulo(-5.0);
                    break;
                case 33:
                case 37:
                    girarPorAngulo(-15.0);
                    break;
                default:
                    break;
            }
        }

        sleep(200);
        moverPorTempo(0.5, 600);
    }

    private void lancarNectares() {
        telemetry.addData("Fase", "2 - A lançar néctares");
        telemetry.update();

        motorIt.setPower(0.8);
        sleep(600);

        servoitDir.setPower(1.0);
        servoitEsq.setPower(1.0);
        sleep(2500);

        servoitDir.setPower(0.0);
        servoitEsq.setPower(0.0);
        motorIt.setPower(0.0);
    }

    private void estacionarRobo() {
        telemetry.addData("Fase", "3 - A estacionar");
        telemetry.update();

        if (tagConfirmada >= 30 && tagConfirmada <= 33) {
            moverPorTempo(-0.5, 400);
            sleep(150);
            girarPorAngulo(90.0);
            sleep(150);
            moverPorTempo(0.6, 1200);

        } else if (tagConfirmada >= 34 && tagConfirmada <= 37) {
            moverPorTempo(-0.5, 400);
            sleep(150);
            girarPorAngulo(-90.0);
            sleep(150);
            moverPorTempo(0.6, 1200);

        } else {
            moverPorTempo(0.5, 1000);
        }

        pararMotores();
        telemetry.addData("Status", "Autónomo Concluído!");
        telemetry.update();
    }

    private void girarPorAngulo(double grausAlvo) {
        double anguloInicial = getAnguloAtual();
        double erro = grausAlvo;

        runtime.reset();
        while (opModeIsActive() && Math.abs(erro) > 1.5 && runtime.seconds() < 3.0) {
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

    private void moverPorTempo(double potencia, long tempoMs) {
        runtime.reset();
        while (opModeIsActive() && runtime.milliseconds() < tempoMs) {
            motorDir.setPower(potencia);
            motorEsq.setPower(potencia);
        }
        pararMotores();
    }

    private void pararMotores() {
        motorDir.setPower(0.0);
        motorEsq.setPower(0.0);
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