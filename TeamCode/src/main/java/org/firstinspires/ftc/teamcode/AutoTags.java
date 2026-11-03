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

        telemetry.addData("Estado", "Pronto para iniciar");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Atualiza os dados da câmara a cada ciclo
            tagWebCam.update();

            // Procurar pela Tag pelo ID pretendido (ex: ID 20)
            AprilTagDetection minhaTag = tagWebCam.getTagBySpecificId(20);

            if (minhaTag != null && minhaTag.ftcPose != null) {
                telemetry.addData("Tag Encontrada!", "ID: 20");
                telemetry.addData("Distância (X)", minhaTag.ftcPose.x);
                telemetry.addData("Distância (Y)", minhaTag.ftcPose.y);
                telemetry.addData("Distância (Range)", minhaTag.ftcPose.range);
                telemetry.addData("Ângulo (Bearing)", minhaTag.ftcPose.bearing);
            } else {
                telemetry.addData("Estado", "A procurar por AprilTags...");
            }

            telemetry.update();
        }

        tagWebCam.stop();
    }
}