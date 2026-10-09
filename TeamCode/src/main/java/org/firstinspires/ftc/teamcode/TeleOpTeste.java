package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "TeleOpTeste")

public class TeleOpTeste extends OpMode {

    //nomeando as variáveis
    DcMotor motor_dir;
    DcMotor motor_esq;

    @Override
    public void init() {

        //nomeando as variáveis dos motores
        motor_dir = hardwareMap.get(DcMotor.class, "motor_dir");
        motor_esq = hardwareMap.get(DcMotor.class, "motor_esq");

        // Inverte o motor direito para que os dois empurrem o robô para frente
        motor_dir.setDirection(DcMotorSimple.Direction.REVERSE);

        motor_dir.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor_esq.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //mensagem de inicialização
        telemetry.addData("Modo teleoperado", "Ativo!");
        telemetry.update();

    }

    @Override
    public void loop() {

        // AMB0S OS MOTORES
        //quando apertar o botão A, os motores girarão para frente na potência 0.9
        if (gamepad1.a) {

            motor_dir.setPower(0.9);
            motor_esq.setPower(0.9);

            telemetry.addData("Motores", "ativos em +0.9");

        }

        //quando apertar o botão Y, os motores girarão para trás na potência -0.8
        else if (gamepad1.y){

            motor_dir.setPower(-0.8);
            motor_esq.setPower(-0.8);

            telemetry.addData("Motores", "ativos em -0.8");

        }

        // MOTOR DIREITO
        //se A nem Y estiverem pressionados, roda a lógica individual dos motores e analógicos
        else {

            //quando o B e o R1 estiverem pressionados, o motor direito girará na potência -0.7
            if (gamepad1.b && gamepad1.right_bumper){

                motor_dir.setPower(-0.7);
                telemetry.addData("Motor Direito", "Ativo em -0.7");

            }

            //quando o R1 estiver pressionado, o motor direito girará na potência 0.7
            else if (gamepad1.right_bumper){

                motor_dir.setPower(0.7);
                telemetry.addData("Motor Direito", "Ativo em +0.7");

            }

            //se nenhum botão do lado direito for pressionado, usa o Analógico DIREITO
            else {

                double potencia_dir = -gamepad1.right_stick_y;
                motor_dir.setPower(potencia_dir);
                telemetry.addData("Potência Direita", potencia_dir);

            }


            // MOTOR ESQUERDO
            //quando o B e o L1 estiverem pressionados, o motor esquerdo girará na potência -0.7
            if (gamepad1.b && gamepad1.left_bumper){

                motor_esq.setPower(-0.7);
                telemetry.addData("Motor Esquerdo", "Ativo em -0.7");

            }

            //quando o L1 estiver pressionado, o motor esquerdo girará na potência 0.7
            else if (gamepad1.left_bumper){

                motor_esq.setPower(0.7);
                telemetry.addData("Motor Esquerdo", "Ativo em +0.7");

            }

            //se nenhum botão do lado esquerdo for pressionado, usa o Analógico ESQUERDO
            else {

                double potencia_esq = -gamepad1.left_stick_y;
                motor_esq.setPower(potencia_esq);
                telemetry.addData("Potência Esquerda", potencia_esq);

            }

        }

        // Atualiza a telemetria uma única vez por ciclo de loop
        telemetry.update();

    }

}