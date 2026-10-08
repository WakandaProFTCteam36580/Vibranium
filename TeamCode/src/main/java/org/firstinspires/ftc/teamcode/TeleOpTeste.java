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



        //quando apertar o botão A, os motores girarão para frente na potência 0.8
        if (gamepad1.a) {

            motor_dir.setPower(1);
            motor_esq.setPower(1);

            telemetry.addData("Motores", "ativos em +0.8");
            telemetry.update();

        }

        //quando o if nõ está sendo executado, os motores param imediatamente
        else{

            motor_dir.setPower(0);
            motor_esq.setPower(0);

            telemetry.addData("Motores", "parados");
            telemetry.update();

        }

        //quando apertar o botão Y, os motores girarão para trás na potência -0.8
        if (gamepad1.y){

            motor_dir.setPower(-0.9);
            motor_esq.setPower(-0.9);

            telemetry.addData("Motores", "ativos em -0.8");
            telemetry.update();

        }

        //quando o if nõ está sendo executado, os motores param imediatamente
        else{

            motor_dir.setPower(0);
            motor_esq.setPower(0);

            telemetry.addData("Motores", "parados");
            telemetry.update();

        }

        //quando o B e o R1 estverem precionados, o motor direito girará na potência -0.5
        if(gamepad1.b && gamepad1.right_bumper){

            motor_dir.setPower(-0.5);

            telemetry.addData("Motor Direito", "Ativo em -0.5");
            telemetry.update();

        }

        //quando o if nõ está sendo executado, o motor direito para imediatamente
        else{

            motor_dir.setPower(0);

            telemetry.addData("Motor Direito", "Parado");
            telemetry.update();

        }

        //quando o B e o L1 estiverem precionados, o motor esquerdo girará na potência -0.5
        if (gamepad1.b && gamepad1.left_bumper){

            motor_esq.setPower(-0.5);

            telemetry.addData("Motor Esquerdo", "Ativo em -0.5");
            telemetry.update();

        }

        //quando o if nõ está sendo executado, o motor esquerdo para imediatamente
        else{

            motor_esq.setPower(0);

            telemetry.addData("Motor Esquerdo", "Parado");
            telemetry.update();

        }

        //quando o R1 estiver precionado, o motor direito girará na potência 0.5
        if (gamepad1.right_bumper){

            motor_dir.setPower(0.5);

            telemetry.addData("Motor Direito", "Ativo em +0.5");
            telemetry.update();

        }

        //quando o if nõ está sendo executado, o motor direito para imediatamente
        else{

            motor_dir.setPower(0);

            telemetry.addData("Motor Direito", "Parado");
            telemetry.update();

        }

        //quando o L1 estiver precionado, o motor esquerdo girará na potência 0.5
        if (gamepad1.left_bumper){

            motor_esq.setPower(0.5);

            telemetry.addData("Motor Esquerdo", "Ativo em +0.5");
            telemetry.update();

        }

        //quando o if nõ está sendo executado, o motor esquerdo para imediatamente
        else{

            motor_esq.setPower(0);

            telemetry.addData("Motor Esquerdo", "Parado");
            telemetry.update();
        }

    }

}