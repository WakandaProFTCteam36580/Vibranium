package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.PwmControl;

@TeleOp(name = "TeleOpTeste")

public class TeleOpTeste extends OpMode {

    //Nomeando as variáveis
    DcMotor motor_dir; //Porta de motor 0
    DcMotor motor_esq; //Porta de motor 1
    DcMotor motor_it; //Porta de motor 2
    DcMotor motor_sh; //Porta de motor 3

    CRServo servoit_esq; //Porta de servos 0
    CRServo servoit_dir; //Porta de servos 1
    CRServo servo_lift; //Porta de servos 2

    @Override
    public void init() {
        //IMPORTANDO AS VARIÁVEIS DOS SERVOS E DOS MOTORES
        motor_dir = hardwareMap.get(DcMotor.class, "motor_dir");
        motor_esq = hardwareMap.get(DcMotor.class, "motor_esq");
        motor_it = hardwareMap.get(DcMotor.class, "motor_it");
        motor_sh = hardwareMap.get(DcMotor.class, "motor_sh");

        //INVERTENDO O SENTIDO DO MOTOR DIREITO
        motor_dir.setDirection(DcMotorSimple.Direction.REVERSE);
        motor_it.setDirection(DcMotorSimple.Direction.REVERSE);
        motor_sh.setDirection(DcMotorSimple.Direction.REVERSE);

        //IMPORTANDO AS VARIÁVEIS DOS SERVOS
        servoit_dir = hardwareMap.get(CRServo.class, "servoit_dir");
        servoit_esq = hardwareMap.get(CRServo.class, "servoit_esq");
        servo_lift = hardwareMap.get(CRServo.class, "servo_lift");

        //EXPANDINDO O RANGE PWM PARA OS SERVOS SERVOS
        ((CRServoImplEx) servoit_dir).setPwmRange(new PwmControl.PwmRange(500, 2500));
        ((CRServoImplEx) servoit_esq).setPwmRange(new PwmControl.PwmRange(500, 2500));

        //INVERTENDO SENTIDO DOS SERVOS
        servoit_dir.setDirection(DcMotorSimple.Direction.REVERSE);
        servo_lift.setDirection(DcMotorSimple.Direction.REVERSE);

        //MENSAGEM DE INICIALIZAÇÃO
        telemetry.addData("Modo teleoperado", "Ativo!");
        telemetry.update();
    }

    @Override
    public void loop() {

        //GAMEPAD 1

        //CONFIGURAÇÃO DOS MOTORES DE TRAÇÃO JUNTOS
        //CONTROLE DOS MOTORES DE TRAÇÃO POSITIVAMENTE
        if (gamepad1.a) {
            motor_dir.setPower(0.9);
            motor_esq.setPower(0.9);
            telemetry.addData("Motores", "ativos em +0.9");
        }
        //CONTROLE DOS MOTORES DE TRAÇÃO NEGATIVAMENTE
        else if (gamepad1.y) {
            motor_dir.setPower(-0.9);
            motor_esq.setPower(-0.9);
            telemetry.addData("Motores", "ativos em -0.9");
        }


        //CONFIGURAÇÃO DO MOTOR DIREITO
        else {
            //CONTROLE DO MOTOR DE TRAÇÃO DIREITO EM -0.6
            if (gamepad1.b && gamepad1.right_bumper) {
                motor_dir.setPower(-0.6);
                telemetry.addData("Motor Direito", "Ativo em -0.6");
            }
            //CONTROLE DO MOTOR DE TRAÇÃO DIREITO EM +0.6
            else if (gamepad1.right_bumper) {
                motor_dir.setPower(0.6);
                telemetry.addData("Motor Direito", "Ativo em +0.6");
            }
            //CONTROLE DO MOTOR DE TRAÇÃO DIREITO NO ANALÓGICO
            else {
                double potencia_dir = -gamepad1.right_stick_y;
                motor_dir.setPower(potencia_dir);
                telemetry.addData("Potência Direita", potencia_dir);
            }


            //CONFIGURAÇÃO DO MOTOR ESQUERDO
            //CONTROLE DO MOTOR DE TRAÇÃO ESQUERDO EM -0.6
            if (gamepad1.b && gamepad1.left_bumper) {
                motor_esq.setPower(-0.6);
                telemetry.addData("Motor Esquerdo", "Ativo em -0.6");
            }
            //CONTROLE DO MOTOR DE TRAÇÃO ESQUERDO EM +0.6
            else if (gamepad1.left_bumper) {
                motor_esq.setPower(0.6);
                telemetry.addData("Motor Esquerdo", "Ativo em +0.6");
            }
            //CONTROLE DO MOTOR DE TRAÇÃO ESQUERDO NO ANALÓGICO
            else {
                double potencia_esq = -gamepad1.left_stick_y;
                motor_esq.setPower(potencia_esq);
                telemetry.addData("Potência Esquerda", potencia_esq);
            }
        }

        //GAMEPAD 2

        //CONTROLE DO SHOOTER
        if (gamepad2.right_bumper) {
            motor_sh.setPower(0.1);
        }
        else if (gamepad2.left_bumper) {
            motor_sh.setPower(-0.1);
        }
        else {
            motor_sh.setPower(0);
        }

        //CONFIGURAÇÃO DO INTAKE (SERVOS E MOTOR)
        if (gamepad2.a) {
            servoit_esq.setPower(1.0);
            servoit_dir.setPower(1.0);
            motor_it.setPower(1.0);
            telemetry.addData("Intake", "Ativo!");
        }
        else if (gamepad2.b) {
            motor_it.setPower(-1.0);
        }
        else {
            servoit_esq.setPower(0);
            servoit_dir.setPower(0);
            motor_it.setPower(0);
        }

        //CONFIGURANDO O SERVO DO LIFT
        if (gamepad2.x) {
            servo_lift.setPower(1.0);
        }

        //Mensagem de atualização
        telemetry.update();
    }
}