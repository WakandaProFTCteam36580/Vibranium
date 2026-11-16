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
        tagWebCam.init(hardwareMap, telemetry);
        telemetry.setMsTransmissionInterval(50);

        telemetry.addData("Estado", "Pronto para iniciar");
        telemetry.update();

        waitForStart();

        int[] meusIds = {30, 31, 32, 33};

        while (opModeIsActive()) {
            tagWebCam.update();

            AprilTagDetection minhaTag = null;
            int idEncontrado = -1;

            for (int id : meusIds) {
                minhaTag = tagWebCam.getTagBySpecificId(id);
                if (minhaTag != null) {
                    idEncontrado = id;
                    break;
                }
            }

            if (minhaTag != null) {
                telemetry.addData("SUCESSO! Tag ID", idEncontrado);
                if (minhaTag.ftcPose != null) {
                    telemetry.addData("Distância (X, Y)", "%.1f cm, %.1f cm", minhaTag.ftcPose.x, minhaTag.ftcPose.y);
                    telemetry.addData("Ângulo (Yaw)", "%.1f graus", minhaTag.ftcPose.yaw);
                }
            } else {
                telemetry.addData("Estado", "A procurar pelas tags (30-33)...");
            }

            telemetry.update();
        }

        tagWebCam.stop();
    }
}