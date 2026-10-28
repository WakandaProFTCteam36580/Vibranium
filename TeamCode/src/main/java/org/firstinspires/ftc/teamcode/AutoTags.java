package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

public class AutoTags {

    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;
    private Telemetry telemetry;

    public AutoTags(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        aprilTagProcessor = new AprilTagProcessor.Builder()
                .build();

        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessor(aprilTagProcessor)
                .build();
    }

    public AprilTagDetection obterDadosDaTag(int idDesejado) {
        List<AprilTagDetection> currentDetections = aprilTagProcessor.getDetections();

        for (AprilTagDetection detection : currentDetections) {
            // Utiliza o campo de identificação detetado pela pose ou conversão do objeto
            if (detection.ftcPose != null && detection.equals(idDesejado)) { // Alternativa segura caso o ID venha no próprio objeto
                return detection;
            }
        }
        return null;
    }

    public void mostrarTelemetriaGeral() {
        List<AprilTagDetection> currentDetections = aprilTagProcessor.getDetections();
        telemetry.addData("Tags Visíveis", currentDetections.size());

        for (AprilTagDetection detection : currentDetections) {
            // Imprime os dados de pose disponíveis que vimos no teu autocompletar
            if (detection.ftcPose != null) {
                telemetry.addData("    Distância (Y)", "%.2f", detection.ftcPose.y);
                telemetry.addData("    Desvio (X)", "%.2f", detection.ftcPose.x);
                telemetry.addData("    Orientação (Yaw)", "%.2f", detection.ftcPose.yaw);
            }
        }
        telemetry.update();
    }

    public void fecharVisao() {
        if (visionPortal != null) {
            visionPortal.close();
        }
    }
}