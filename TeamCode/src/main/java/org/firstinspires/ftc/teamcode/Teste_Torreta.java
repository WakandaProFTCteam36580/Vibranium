package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.drive.AprilTagWebCam;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

@Autonomous(name = "Autonomo Teste Torreta", group = "Testes")
public class Teste_Torreta extends LinearOpMode {

    // Câmara e AprilTags
    private final AprilTagWebCam tagWebCam = new AprilTagWebCam();
    private int tagConfirmada = -1;
    private double anguloTagYaw = 0.0;
    private boolean leuAnguloValido = false;

    // Motor da Torreta
    private DcMotor motorTorreta;

    // Temporizador
    private final ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        // 1. Inicializa o motor da torreta e a câmara
        motorTorreta = hardwareMap.get(DcMotor.class, "motor_torreta");
        motorTorreta.setDirection(DcMotorSimple.Direction.FORWARD);
        motorTorreta.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        tagWebCam.init(hardwareMap, telemetry);

        telemetry.addData("Status", "Pronto para testar a torreta com AprilTags...");
        telemetry.update();

        // 2. Loop durante o INIT: lê continuamente as tags 30 a 37
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

            // Mostra no Driver Hub o que está a ver
            if (tagConfirmada != -1) {
                telemetry.addData("Tag Detetada", tagConfirmada);
                telemetry.addData("Ângulo Yaw", "%.1f graus", anguloTagYaw);
            } else {
                telemetry.addData("Estado", "À procura de AprilTags (30-37)...");
            }
            telemetry.update();
        }

        // Aguarda o PLAY
        waitForStart();

        if (opModeIsActive()) {
            telemetry.addData("Fase", "A alinhar torreta...");
            telemetry.update();

            // Se leu um ângulo válido, ajusta a torreta proporcionalmente ao yaw
            if (leuAnguloValido && Math.abs(anguloTagYaw) > 1.5) {
                // Fator de conversão: ajusta o multiplicador (ex: 15.0) se a torreta rodar demais ou de menos
                long tempoMovimento = (long) (Math.abs(anguloTagYaw) * 15.0);
                double potenciaTorreta = (anguloTagYaw > 0) ? 0.3 : -0.3;

                runtime.reset();
                while (opModeIsActive() && runtime.milliseconds() < Math.min(tempoMovimento, 1500)) {
                    motorTorreta.setPower(potenciaTorreta);
                }
            } else {
                // Caso não tenha lido o yaw, faz um pequeno movimento fixo de teste baseado na tag
                if (tagConfirmada >= 30 && tagConfirmada <= 33) {
                    girarTorretaPorTempo(0.3, 300);
                } else if (tagConfirmada >= 34 && tagConfirmada <= 37) {
                    girarTorretaPorTempo(-0.3, 300);
                }
            }

            // Para a torreta
            motorTorreta.setPower(0.0);
            tagWebCam.stop();

            telemetry.addData("Status", "Teste de alinhamento concluído!");
            telemetry.update();

            // Mantém o opmode aberto para veres a telemetria final
            sleep(2000);
        }
    }

    private void girarTorretaPorTempo(double potencia, long tempoMs) {
        runtime.reset();
        while (opModeIsActive() && runtime.milliseconds() < tempoMs) {
            motorTorreta.setPower(potencia);
        }
        motorTorreta.setPower(0.0);
    }
}