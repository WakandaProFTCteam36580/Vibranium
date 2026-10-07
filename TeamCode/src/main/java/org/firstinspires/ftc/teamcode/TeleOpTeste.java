package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "TeleOpTeste")
public class TeleOpTeste extends OpMode {

    DcMotor motor_dir;
    DcMotor motor_esq;

    @Override
    public void init() {
        motor_dir = hardwareMap.get(DcMotor.class, "motor_dir");
        motor_esq = hardwareMap.get(DcMotor.class, "motor_esq");

        // Inverte um dos motores para que os dois empurrem o robô para frente
        // (Você pode precisar trocar para o motor_esq dependendo da sua montagem física)
        motor_dir.setDirection(DcMotorSimple.Direction.REVERSE);

        telemetry.addData("Modo teleoperado", "Ativo!");
        telemetry.update();
    }

    @Override
    public void loop() {
        // Analógico ESQUERDO para cima/baixo controla o motor esquerdo
        // Nota: O eixo Y é invertido no gamepad por padrão, por isso o sinal de menos (-)
        double potencia_esq = -gamepad1.left_stick_y;
        motor_esq.setPower(potencia_esq);

        // Analógico DIREITO para cima/baixo controla o motor direito
        double potencia_dir = -gamepad1.right_stick_y;
        motor_dir.setPower(potencia_dir);

        // Mensagens agrupadas para o Driver Hub
        telemetry.addData("Potência Esquerda", potencia_esq);
        telemetry.addData("Potência Direita", potencia_dir);
        telemetry.update();
    }
}