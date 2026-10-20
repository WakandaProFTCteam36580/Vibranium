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

        // 1. Travessia inicial expandida para 140 polegadas
        Trajectory travessia140 = drive.trajectoryBuilder(startPose)
                .forward(140)
                .build();

        telemetry.addData("Status", "Robô pronto para o arranque!");
        telemetry.update();


        // --- 2. ESPERA DO START ---

        waitForStart();

        if (isStopRequested()) return;


        // --- 3. FASE DE EXECUÇÃO ---

        // Step 1: Avança 140 polegadas até cruzar a posição correta
        drive.followTrajectory(travessia140);

        // Step 2: Giro de 180°
        girarPorTempo(drive, -0.4, 0.4, 1700);
        drive.setPoseEstimate(new Pose2d(drive.getPoseEstimate().getX(), drive.getPoseEstimate().getY(), Math.toRadians(180)));

        // Step 3: Espera 5 segundos
        sleep(5000);

        // Step 4: Virar para a DIREITA em 90°
        girarPorTempo(drive, 0.4, -0.4, 950);
        drive.setPoseEstimate(new Pose2d(drive.getPoseEstimate().getX(), drive.getPoseEstimate().getY(), Math.toRadians(270)));

        // Step 5: Andar para a frente, 35 polegadas
        Trajectory avanco1 = drive.trajectoryBuilder(drive.getPoseEstimate())
                .forward(35)
                .build();
        drive.followTrajectory(avanco1);

        // Step 6: Virar para a ESQUERDA em 90°
        girarPorTempo(drive, -0.4, 0.4, 950);
        drive.setPoseEstimate(new Pose2d(drive.getPoseEstimate().getX(), drive.getPoseEstimate().getY(), Math.toRadians(180)));

        // Step 7: Finalizar andando 40 polegadas para a frente
        Trajectory avancoFinal = drive.trajectoryBuilder(drive.getPoseEstimate())
                .forward(40)
                .build();
        drive.followTrajectory(avancoFinal);
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