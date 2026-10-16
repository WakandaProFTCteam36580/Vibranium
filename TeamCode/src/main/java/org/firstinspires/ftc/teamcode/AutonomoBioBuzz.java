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

        SampleTankDrive drive = new SampleTankDrive(hardwareMap);

        // Posição inicial do robô (X: 0, Y: 0, Ângulo: 0°)
        Pose2d startPose = new Pose2d(0, 0, Math.toRadians(0));
        drive.setPoseEstimate(startPose);

        // 1. Constrói a trajetória de travessia inicial (90 polegadas)
        Trajectory travessia = drive.trajectoryBuilder(startPose)
                .forward(90)
                .build();

        telemetry.addData("Status", "Robô pronto para o arranque!");
        telemetry.update();


        // --- 2. ESPERA DO START ---

        waitForStart();

        if (isStopRequested()) return;


        // --- 3. FASE DE EXECUÇÃO ---

        // Executa a travessia reta de 90 polegadas
        drive.followTrajectory(travessia);

        // 2. Giro de 180° Manual (Motor esquerdo para frente, direito para trás)
        // Dica: Ajuste o tempo (ex: 800 ms) no treino de bancada até o robô dar meia-volta completa
        girarPorTempo(drive, 0.4, -0.4, 800);

        // Informa ao Road Runner a nova orientação (-180°) após o giro manual
        Pose2d posPosGiro180 = new Pose2d(drive.getPoseEstimate().getX(), drive.getPoseEstimate().getY(), Math.toRadians(-180));
        drive.setPoseEstimate(posPosGiro180);

        // (Espaço reservado para acionamento de atuadores/lançador)

        // 3. Giro de -90° Manual (Gira mais 90° no mesmo sentido)
        girarPorTempo(drive, 0.4, -0.4, 400);

        // Informa ao Road Runner a nova orientação (-270° / +90°)
        Pose2d posPosGiro90 = new Pose2d(drive.getPoseEstimate().getX(), drive.getPoseEstimate().getY(), Math.toRadians(-270));
        drive.setPoseEstimate(posPosGiro90);

        // 4. Deslocamento final de 25 polegadas até o Jardim
        Trajectory deslocamentoJardim = drive.trajectoryBuilder(drive.getPoseEstimate())
                .forward(25)
                .build();

        drive.followTrajectory(deslocamentoJardim);
    }

    /**
     * Método auxiliar para realizar rotação aplicando potências opostas nos motores por um tempo definido.
     */
    private void girarPorTempo(SampleTankDrive drive, double potEsq, double potDir, long tempoMs) {
        drive.setMotorPowers(potEsq, potDir);
        sleep(tempoMs);
        drive.setMotorPowers(0, 0); // Trava os motores após o tempo estipulado
    }
}