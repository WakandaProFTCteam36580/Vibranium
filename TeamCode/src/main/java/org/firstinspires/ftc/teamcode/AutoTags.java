package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.drive.AprilTagWebCam;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

@Autonomous(name="Teste Tags", group="Autonomous")
public class AutoTags extends LinearOpMode {

    private AprilTagWebCam tagWebCam = new AprilTagWebCam();

    @Override
    public void runOpMode() {
        // Inicializa a câmara passando o hardwareMap e a telemetria
        tagWebCam.init(hardwareMap, telemetry);

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

            // Se encontrou alguma das tags, mostra os dados dela
            if (minhaTag != null && minhaTag.ftcPose != null) {
                telemetry.addData("Tag Encontrada!", "ID: " + idEncontrado);
                telemetry.addData("Distância (X)", minhaTag.ftcPose.x);
                telemetry.addData("Distância (Y)", minhaTag.ftcPose.y);
                telemetry.addData("Distância (Range)", minhaTag.ftcPose.range);
                telemetry.addData("Ângulo (Bearing)", minhaTag.ftcPose.bearing);
            } else {
                telemetry.addData("Estado", "A procurar pelas tags 30 a 33...");
            }

            telemetry.update();
        }

        tagWebCam.stop();
    }
}