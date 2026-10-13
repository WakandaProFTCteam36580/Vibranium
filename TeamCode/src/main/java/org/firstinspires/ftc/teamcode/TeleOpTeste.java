package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "TeleOpTeste")

public class TeleOpTeste extends OpMode {

    //Nomeando as variáveis
    DcMotor motor_dir;
    DcMotor motor_esq;
    CRServo servoit_dir;
    CRServo servoit_esq;

    @Override
    public void init() {

        //Importando as variáveis dos motores
        motor_dir = hardwareMap.get(DcMotor.class, "motor_dir");
        motor_esq = hardwareMap.get(DcMotor.class, "motor_esq");

        // Inverte o sentido do motor direito para que os dois empurrem o robô para frente
        motor_dir.setDirection(DcMotorSimple.Direction.REVERSE);

        //Importando as variáveis dos servos
        servoit_dir = hardwareMap.get(CRServo.class, "servoit_dir");
        servoit_esq = hardwareMap.get(CRServo.class, "servoit_esq");

        // Inverte o sentido do servo direito para que os dois girem no sentido que os pólens sejam capturados
        servoit_dir.setDirection(DcMotorSimple.Direction.REVERSE);

        //mensagem de inicialização
        telemetry.addData("Modo teleoperado", "Ativo!");
        telemetry.update();

    }

    @Override
    public void loop() {

        //quando apertar o botão A, os motores girarão para frente na potência 0.7
        if (gamepad1.a) {

            motor_dir.setPower(0.7);
            motor_esq.setPower(0.7);

            telemetry.addData("Motores", "ativos em +0.7");

        }

        //quando apertar o botão Y, os motores girarão para trás na potência -0.7
        else if (gamepad1.y) {

            motor_dir.setPower(-0.7);
            motor_esq.setPower(-0.7);

            telemetry.addData("Motores", "ativos em -0.7");

        }

        //se A nem Y estiverem pressionados, roda a lógica individual dos motores e analógicos
        else {

            //CONTROLE DO MOTOR DIREITO
            //quando o B e o R1 estiverem pressionados, o motor direito girará na potência -0.5
            if (gamepad1.b && gamepad1.right_bumper) {

                motor_dir.setPower(-0.5);

                telemetry.addData("Motor Direito", "Ativo em -0.5");

            }

            //quando o R1 estiver pressionado, o motor direito girará na potência 0.5
            else if (gamepad1.right_bumper) {

                motor_dir.setPower(0.5);

                telemetry.addData("Motor Direito", "Ativo em +0.5");

            }

            //se nenhum botão do lado direito for pressionado, usa o Analógico DIREITO
            else {

                double potencia_dir = -gamepad1.right_stick_y;

                motor_dir.setPower(potencia_dir);

                telemetry.addData("Potência Direita", potencia_dir);

            }

            //CONTROLE DO MOTOR ESQUERDO
            //quando o B e o L1 estiverem pressionados, o motor esquerdo girará na potência -0.5
            if (gamepad1.b && gamepad1.left_bumper) {

                motor_esq.setPower(-0.5);

                telemetry.addData("Motor Esquerdo", "Ativo em -0.5");

            }

            //quando o L1 estiver pressionado, o motor esquerdo girará na potência 0.5
            else if (gamepad1.left_bumper) {

                motor_esq.setPower(0.5);

                telemetry.addData("Motor Esquerdo", "Ativo em +0.5");

            }

            //se nenhum botão do lado esquerdo for pressionado, usa o Analógico ESQUERDO
            else {

                double potencia_esq = -gamepad1.left_stick_y;
                motor_esq.setPower(potencia_esq);

                telemetry.addData("Potência Esquerda", potencia_esq);

            }

        }

        if (gamepad2.a) {

            servoit_dir.setPower(1.0);

            telemetry.addData("servo direito", "Ativo em +1.0");

        }

        else if (gamepad2.y) {

            servoit_esq.setPower(1.0);

            telemetry.addData("servo esquerdo", "Ativo em +1.0");

        }

        else {

            servoit_dir.setPower(0);
            servoit_esq.setPower(0);

        }

        // Atualiza a mensagem
        telemetry.update();

    }

}