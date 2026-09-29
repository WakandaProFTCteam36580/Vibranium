package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.drive.SampleTankDrive;

@Autonomous(name = "Autonomo BioBuzz", group = "Autonomous")
public class AutonomoBioBuzz extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        // Inicializa a base de tração de 6 rodas
        SampleTankDrive drive = new SampleTankDrive(hardwareMap);

        // Posição inicial do robô (X, Y, Ângulo)
        Pose2d startPose = new Pose2d(0, 0, Math.toRadians(0));
        drive.setPoseEstimate(startPose);

        // Trajetória 1: Avança 30 polegadas
        Trajectory trajetoria1 = drive.trajectoryBuilder(startPose)
                .forward(30)
                .build();

        // Trajetória 2: Avança mais 70 polegadas
        Trajectory trajetoria2 = drive.trajectoryBuilder(trajetoria1.end())
                .forward(70)
                .build();

        telemetry.addData("Status", "Robô pronto para o arranque!");
        telemetry.update();

        waitForStart();

        if (isStopRequested()) return;

        // Executa as trajetórias no robô físico
        drive.followTrajectory(trajetoria1);
        drive.followTrajectory(trajetoria2);
    }
}