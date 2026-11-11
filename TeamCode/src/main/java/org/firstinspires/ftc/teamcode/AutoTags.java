package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.drive.AprilTagWebCam;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

@Autonomous(name="Teste AprilTags", group="Autonomous")
public class AutoTags extends LinearOpMode {

    private AprilTagWebCam tagWebCam = new AprilTagWebCam();

    @Override
    public void runOpMode() {
        // Inicializa a câmara passando o hardwareMap e a telemetria
        tagWebCam.init(hardwareMap, telemetry);

        // Otimiza o envio de telemetria para o Driver Hub (evita lag e atualiza fluidamente)
        telemetry.setMsTransmissionInterval(50);

        telemetry.addData("Estado", "Pronto para iniciar");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Atualiza os dados da câmara a cada ciclo
            tagWebCam.update();

            // Lista das tags que realmente tens disponíveis para teste
            int[] meusIds = {30, 31, 32, 33};
            AprilTagDetection minhaTag = null;
            int idEncontrado = -1;

            // Tenta encontrar qualquer uma das tags da vossa lista
            for (int id : meusIds) {
                minhaTag = tagWebCam.getTagBySpecificId(id);
                if (minhaTag != null && minhaTag.ftcPose != null) {
                    idEncontrado = id;
                    break; // Encontrou uma válida, para o ciclo de procura
                }
            }

            // Se encontrou alguma das tags, mostra os dados detalhados no Driver Hub
            if (minhaTag != null && minhaTag.ftcPose != null) {
                telemetry.addData("Status", "TAG ENCONTRADA!");
                telemetry.addData("ID da Tag", idEncontrado);
                telemetry.addData("Alcance (Range)", "%.2f cm/pol", minhaTag.ftcPose.range);
                telemetry.addData("Bearing (Ângulo)", "%.2f deg", minhaTag.ftcPose.bearing);
                telemetry.addData("Eixo X", "%.2f", minhaTag.ftcPose.x);
                telemetry.addData("Eixo Y", "%.2f", minhaTag.ftcPose.y);
            } else {
                telemetry.addData("Status", "A procurar pelas tags (30-33)...");
            }

            // Envia tudo para o ecrã do Driver Hub
            telemetry.update();
        }

        tagWebCam.stop();
    }
}