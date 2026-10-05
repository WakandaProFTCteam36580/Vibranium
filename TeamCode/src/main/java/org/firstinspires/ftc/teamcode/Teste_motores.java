package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

@Autonomous(name = "Teste Direto dos Motores", group = "Teste")
public class Teste_motores extends LinearOpMode {
    private DcMotor motor_dir;
    private DcMotor motor_esq;

    @Override
    public void runOpMode() {
        motor_dir = hardwareMap.get(DcMotor.class, "motor_dir");
        motor_esq = hardwareMap.get(DcMotor.class, "motor_esq");

        telemetry.addData("Status", "Pronto para iniciar o teste dos motores.");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            // Executa o teste apenas do motor direito
            telemetry.addData("Teste", "Testando MOTOR DIREITO.");
            telemetry.update();
            motor_dir.setPower(0.2);
            sleep(2000);
            motor_dir.setPower(0);
            sleep(1000);

            // Executa o teste apenas do motor esquerdo
            telemetry.addData("Teste", "Iniciando o teste apenas do motor esquerdo.");
            telemetry.update();
            motor_esq.setPower(0.2);
            sleep(2000);
            motor_esq.setPower(0); // Correção: desliga o motor esquerdo
            sleep(1000);

            // Executa o teste dos dois motores em simultâneo
            telemetry.addData("Teste", "Testando os dois motores ao mesmo tempo.");
            telemetry.update();
            motor_esq.setPower(0.2);
            motor_dir.setPower(0.2);
            sleep(4000);

            // Desliga todos os motores ao finalizar
            telemetry.addData("Teste", "Desliga os dois motores ao finalizar.");
            telemetry.update();
            motor_esq.setPower(0);
            motor_dir.setPower(0);

            telemetry.addData("Status", "Teste concluído com sucesso.");
            telemetry.update();
        }
    }
}