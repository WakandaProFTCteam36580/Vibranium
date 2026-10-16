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

        // Configura a base motriz de tração (mapeamento de hardware feito internamente)
        SampleTankDrive drive = new SampleTankDrive(hardwareMap);

        // Posição inicial do robô (X: 0, Y: 0, Ângulo: 0)
        Pose2d startPose = new Pose2d(0, 0, Math.toRadians(0));
        drive.setPoseEstimate(startPose);

        // Constrói a trajetória de travessia inicial
        Trajectory travessia = drive.trajectoryBuilder(startPose)
                .forward(90)
                .build();

        // Mensagem de confirmação na Driver Station
        telemetry.addData("Status", "Robô pronto para o arranque!");
        telemetry.update();


        // --- 2. ESPERA DO START ---

        waitForStart();

        if (isStopRequested()) return;


        // --- 3. FASE DE EXECUÇÃO ---

        // 1. Atravessa as 90 polegadas até o outro lado
        drive.followTrajectory(travessia);

        // 2. Giro de 180° (mantendo +0.001 em X para evitar segmento nulo)
        Pose2d posAtual = drive.getPoseEstimate();
        Trajectory giro180 = drive.trajectoryBuilder(posAtual)
                .lineToLinearHeading(new Pose2d(posAtual.getX() + 0.001, posAtual.getY(), Math.toRadians(-180)))
                .build();

        drive.followTrajectory(giro180);

        // (Espaço reservado para o acionamento do mecanismo/lançador)

        // 3. Giro de -90° absoluto no plano do campo
        Pose2d posAtual2 = drive.getPoseEstimate();
        Trajectory giro90 = drive.trajectoryBuilder(posAtual2)
                .lineToLinearHeading(new Pose2d(posAtual2.getX() + 0.001, posAtual2.getY(), Math.toRadians(-90)))
                .build();

        drive.followTrajectory(giro90);

        // 4. Deslocamento final de 25 polegadas até o Jardim
        Trajectory deslocamentoJardim = drive.trajectoryBuilder(drive.getPoseEstimate())
                .forward(25)
                .build();

        drive.followTrajectory(deslocamentoJardim);
    }
}