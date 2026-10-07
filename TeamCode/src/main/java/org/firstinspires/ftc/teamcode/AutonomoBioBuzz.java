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

        // --- 1. FASE DE INICIALIZAÇÃO (INIT) ---

        // Configura a base motriz de tração
        SampleTankDrive drive = new SampleTankDrive(hardwareMap);

        // Posição inicial do robô (X: 0, Y: 0, Ângulo: 0)
        Pose2d startPose = new Pose2d(0, 0, Math.toRadians(0));
        drive.setPoseEstimate(startPose);

        // Constrói a trajetória de travessia antes do start
        Trajectory travessia = drive.trajectoryBuilder(startPose)
                .forward(90)
                .build();

        // Mensagem de confirmação para os pilotos
        telemetry.addData("Status", "Robô pronto para o arranque!");
        telemetry.update();


        // --- 2. ESPERA DO START ---

        waitForStart();

        if (isStopRequested()) return;


        // --- 3. FASE DE EXECUÇÃO ---

        // Atravessa os 90 polegadas até o outro lado
        drive.followTrajectory(travessia);

        // Obtém a posição atual do robô no campo
        Pose2d posAtual = drive.getPoseEstimate();

// Cria uma trajetória mantendo o mesmo X e Y, mas mudando o ângulo para -180°
        Trajectory giro180 = drive.trajectoryBuilder(posAtual)
                .lineToLinearHeading(new Pose2d(posAtual.getX(), posAtual.getY(), Math.toRadians(-180)))
                .build();

        drive.followTrajectory(giro180);

        // (Espaço reservado para o acionamento do mecanismo/lançador)

        Pose2d posAtual2 = drive.getPoseEstimate();

        Trajectory giro90 = drive.trajectoryBuilder(posAtual2)
                .lineToLinearHeading(new Pose2d(posAtual2.getX(), posAtual2.getY(), Math.toRadians(-90)))
                .build();

        drive.followTrajectory(giro90);

        // Constrói o deslocamento final lendo a posição atual do robô
        Trajectory deslocamentoJardim = drive.trajectoryBuilder(drive.getPoseEstimate())
                .forward(25)
                .build();

        drive.followTrajectory(deslocamentoJardim);
    }
}