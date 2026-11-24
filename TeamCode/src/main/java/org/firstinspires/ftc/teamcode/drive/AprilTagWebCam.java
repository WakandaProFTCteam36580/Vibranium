package org.firstinspires.ftc.teamcode.drive;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagLibrary;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.ArrayList;
import java.util.List;

public class AprilTagWebCam {
    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;

    private List<AprilTagDetection> detectedTags = new ArrayList<>();
    private Telemetry telemetry;

    public void init(HardwareMap hwMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        // Regista todas as 8 tags do campo (30 a 33 e 34 a 37)
        AprilTagLibrary minhaBiblioteca = new AprilTagLibrary.Builder()
                .addTag(30, "Tag 30", 10.0, DistanceUnit.CM)
                .addTag(31, "Tag 31", 10.0, DistanceUnit.CM)
                .addTag(32, "Tag 32", 10.0, DistanceUnit.CM)
                .addTag(33, "Tag 33", 10.0, DistanceUnit.CM)
                .addTag(34, "Tag 34", 10.0, DistanceUnit.CM)
                .addTag(35, "Tag 35", 10.0, DistanceUnit.CM)
                .addTag(36, "Tag 36", 10.0, DistanceUnit.CM)
                .addTag(37, "Tag 37", 10.0, DistanceUnit.CM)
                .build();

        aprilTagProcessor = new AprilTagProcessor.Builder()
                .setTagLibrary(minhaBiblioteca)
                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                .setDrawTagID(true)
                .setDrawTagOutline(true)
                .setDrawAxes(true)
                .setDrawCubeProjection(true)
                .setOutputUnits(DistanceUnit.CM, AngleUnit.DEGREES)
                .build();

        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hwMap.get(WebcamName.class, "Webcam 1"));
        builder.setCameraResolution(new Size(640, 480));
        builder.addProcessor(aprilTagProcessor);

        visionPortal = builder.build();
    }

    public void update() {
        detectedTags = aprilTagProcessor.getDetections();
    }

    public List<AprilTagDetection> getDetectedTags() {
        return detectedTags;
    }

    public AprilTagSingleDetection getFirstDetectedTag() {
        for (AprilTagDetection detection : detectedTags) {
            if (detection instanceof AprilTagSingleDetection) {
                return (AprilTagSingleDetection) detection;
            }
        }
        return null;
    }

    public void stop() {
        if (visionPortal != null) {
            visionPortal.close();
        }
    }
}