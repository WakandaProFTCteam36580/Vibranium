package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "TeleOpTeste")

public class TeleOpTeste extends OpMode {

    //nomenando as variáveis
    DcMotor motor_dir;
    DcMotor motor_esq;

    @Override
    public void init() {

        //nomeando as variáveis dos motores
        motor_dir = hardwareMap.get(DcMotor.class, "motor_dir");
        motor_esq = hardwareMap.get(DcMotor.class, "motor_esq");

        // Inverte um dos motores para que os dois empurrem o robô para frente
        motor_dir.setDirection(DcMotorSimple.Direction.REVERSE);

        motor_dir.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor_esq.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        //mensagem de inicialização
        telemetry.addData("Modo teleoperado", "Ativo!");
        telemetry.update();

    }

    @Override
    public void loop() {

        // Analógico ESQUERDO para cima/baixo controla o motor esquerdo
        double potencia_esq = -gamepad1.left_stick_y;

        motor_esq.setPower(potencia_esq);

        // Analógico DIREITO para cima/baixo controla o motor direito
        double potencia_dir = -gamepad1.right_stick_y;

        motor_dir.setPower(potencia_dir);

        // Mensagens agrupadas para o Driver Hub
        telemetry.addData("Potência Esquerda", potencia_esq);
        telemetry.addData("Potência Direita", potencia_dir);
        telemetry.update();

        if (gamepad1.a) {

            motor_dir.setPower(0.8);

            motor_esq.setPower(0.8);

        }

        else{

            motor_dir.setPower(0);

            motor_esq.setPower(0);


        }

        if (gamepad1.y){

            motor_dir.setPower(-0.8);

            motor_esq.setPower(-0.8);

        }

        else{

            motor_dir.setPower(0);

            motor_esq.setPower(0);

        }


        if (gamepad1.right_bumper){

            motor_dir.setPower(0.5);

        }

        else {

            motor_dir.setPower(0);

        }

        if (gamepad1.left_bumper){

            motor_esq.setPower(0.5);

        }

        else {

            motor_esq.setPower(0);

        }

    }

}