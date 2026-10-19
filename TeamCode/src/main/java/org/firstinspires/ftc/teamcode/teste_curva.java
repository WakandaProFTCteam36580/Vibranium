package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.drive.SampleTankDrive;

@Autonomous(name = "Teste Curva", group = "Teste")
public class teste_curva extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {

        SampleTankDrive drive = new SampleTankDrive(hardwareMap);

        telemetry.addData("Status", "Alinhe o robô em uma linha do tapete e pressione PLAY.");
        telemetry.update();

        waitForStart();

        if (isStopRequested()) return;

        // =======================================================
        // BATERIA DE TESTES: GIRO DE 180°
        // =======================================================

        // Tentativa 1: 1500 ms
        executarGiroComPausa(drive, "180° - 1500 ms", -0.4, 0.4, 1500);

        // Tentativa 2: 1700 ms
        executarGiroComPausa(drive, "180° - 1700 ms", -0.4, 0.4, 1700);

        // Tentativa 3: 1900 ms
        executarGiroComPausa(drive, "180° - 1900 ms", -0.4, 0.4, 1900);


        // =======================================================
        // BATERIA DE TESTES: GIRO DE 90° (ESQUERDA)
        // =======================================================

        // Tentativa 1: 750 ms
        executarGiroComPausa(drive, "90° ESQ - 750 ms", -0.4, 0.4, 750);

        // Tentativa 2: 850 ms
        executarGiroComPausa(drive, "90° ESQ - 850 ms", -0.4, 0.4, 850);

        // Tentativa 3: 950 ms
        executarGiroComPausa(drive, "90° ESQ - 950 ms", -0.4, 0.4, 950);


        telemetry.addData("Status", "Bateria de testes concluída!");
        telemetry.update();
    }

    /**
     * Executa o giro, exibe o tempo na Driver Station e aguarda 4 segundos para avaliação visual.
     */
    private void executarGiroComPausa(SampleTankDrive drive, String testeNome, double potEsq, double potDir, long tempoMs) {
        telemetry.addData("Executando", testeNome);
        telemetry.addData("Dica", "Observe o alinhamento com as linhas da arena...");
        telemetry.update();

        drive.setMotorPowers(potEsq, potDir);
        sleep(tempoMs);
        drive.setMotorPowers(0, 0);

        // Pausa de 4 segundos para alinhar o robô visualmente antes da próxima tentativa
        sleep(4000);
    }
}