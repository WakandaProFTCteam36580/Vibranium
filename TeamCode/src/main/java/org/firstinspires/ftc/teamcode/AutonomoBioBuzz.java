package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.drive.SampleTankDrive;

@Autonomous(name = "Autonomo BioBuzz Giroscopio", group = "Autonomous")
public class AutonomoBioBuzz extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {

        // 1. FASE DE INICIALIZAÇÃO
        SampleTankDrive drive = new SampleTankDrive(hardwareMap);

        Pose2d startPose = new Pose2d(0, 0, Math.toRadians(0));
        drive.setPoseEstimate(startPose);

        // Avanço inicial até a Colmeia
        Trajectory travessia140 = drive.trajectoryBuilder(startPose)
                .forward(140)
                .build();

        telemetry.addData("Status", "Robô e Giroscópio (IMU) inicializados!");
        telemetry.update();


        // ESPERA DO START
        waitForStart();

        if (isStopRequested()) return;


        // 3. FASE DE EXECUÇÃO

        // Avança até cruzar a posição da Colmeia
        drive.followTrajectory(travessia140);

        // : Giro de 180° exatos via Giroscópio
        drive.girarPorAngulo(180, 0.4);
        drive.setPoseEstimate(new Pose2d(drive.getPoseEstimate().getX(), drive.getPoseEstimate().getY(), Math.toRadians(180)));


        // =======================================================
        // ROTINA DE LANÇAMENTO DOS PÓLENS NA COLMEIA
        // =======================================================
        telemetry.addData("Status", "Lançando Pólens na Colmeia...");
        telemetry.update();

        // Ligar o Shooter a 100% de potência
        drive.motor_sh.setPower(1.0);
        sleep(1000); // 1 segundo para acelerar o volante

        // Ligar Intake e Servos para alimentar os pólens
        drive.motor_it.setPower(1.0);
        drive.servoit_esq.setPower(1.0);
        drive.servoit_dir.setPower(-1.0);

        sleep(4000); // Lança os pólens durante 4 segundos

        // Desligar os mecanismos
        drive.motor_sh.setPower(0.0);
        drive.motor_it.setPower(0.0);
        drive.servoit_esq.setPower(0.0);
        drive.servoit_dir.setPower(0.0);
        //


        // Virar 90° para a ESQUERDA via Giroscópio
        drive.girarPorAngulo(90, 0.4);
        drive.setPoseEstimate(new Pose2d(drive.getPoseEstimate().getX(), drive.getPoseEstimate().getY(), Math.toRadians(270)));

        // Andar 35 polegadas para a frente
        Trajectory avanco1 = drive.trajectoryBuilder(drive.getPoseEstimate())
                .forward(35)
                .build();
        drive.followTrajectory(avanco1);

        // Virar 90° para a DIREITA via Giroscópio (-90)
        drive.girarPorAngulo(-90, 0.4);
        drive.setPoseEstimate(new Pose2d(drive.getPoseEstimate().getX(), drive.getPoseEstimate().getY(), Math.toRadians(180)));

        // Finalizar andando 40 polegadas para a frente
        Trajectory avancoFinal = drive.trajectoryBuilder(drive.getPoseEstimate())
                .forward(40)
                .build();
        drive.followTrajectory(avancoFinal);

        telemetry.addData("Status", "Autônomo Concluído com Giroscópio!");
        telemetry.update();
    }
}