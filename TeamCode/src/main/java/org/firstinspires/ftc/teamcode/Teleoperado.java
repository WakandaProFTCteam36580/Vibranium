package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "TeleOpTeste")

public class Teleoperado extends OpMode {

    //Nomeando as variáveis
    DcMotor motor_dir; //Porta de motor 0 (312 RPM)
    DcMotor motor_esq; //Porta de motor 1 (312 RPM)
    DcMotor motor_it; //Porta de motor 2 (117 RPM)
    DcMotor motor_sh; //Porta de motor 3 (6 000 RPM)

    CRServo servoit_esq; //Porta de servos 0 (Speed)
    CRServo servoit_dir; //Porta de servos 1 (Speed)
    CRServo servo_lift; //Porta de servos 2 (Torque)
    Servo servo_catraca; //Porta de servo 3 (Torque)

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
        servo_catraca = hardwareMap.get(Servo.class, "servo_catraca");

        //EXPANDINDO O RANGE PWM PARA OS SERVOS SERVOS
        ((CRServoImplEx) servoit_dir).setPwmRange(new PwmControl.PwmRange(500, 2500));
        ((CRServoImplEx) servoit_esq).setPwmRange(new PwmControl.PwmRange(500, 2500));
    
        //INVERTENDO SENTIDO DOS SERVOS
        servoit_dir.setDirection(CRServo.Direction.REVERSE);
        servo_lift.setDirection(CRServo.Direction.REVERSE);
        servo_catraca.setDirection(Servo.Direction.REVERSE);

        //MENSAGEM DE INICIALIZAÇÃO
        telemetry.addData("Modo teleoperado", "ativo!");
        telemetry.update();
    }



    @Override
    public void loop() {

        //GAMEPAD 1

        //CONFIGURAÇÃO DOS MOTORES DE TRAÇÃO JUNTOS
        //CONTROLE DOS MOTORES DE TRAÇÃO POSITIVAMENTE
        if (gamepad1.a) {
            motor_dir.setPower(1.0);
            motor_esq.setPower(1.0);
            telemetry.addData("Motores", "POSITIVOS ativos simultâneamente");
        }
        //CONTROLE DOS MOTORES DE TRAÇÃO NEGATIVAMENTE
        else if (gamepad1.y) {
            motor_dir.setPower(-0.9);
            motor_esq.setPower(-0.9);
            telemetry.addData("Motores", "NEGATIVOS ativos simultâneamente");
        }


        //CONFIGURAÇÃO DO MOTOR DIREITO
        else {

            //CONTROLE DO MOTOR DE TRAÇÃO DIREITO EM -0.6
            if (gamepad1.b && gamepad1.right_bumper) {
                motor_dir.setPower(-0.6);
                telemetry.addData("Motor Direito", "em -0.6");
            }
            //CONTROLE DO MOTOR DE TRAÇÃO DIREITO EM +0.6
            else if (gamepad1.right_bumper) {
                motor_dir.setPower(0.6);
                telemetry.addData("Motor Direito", "em +0.6");
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
                telemetry.addData("Motor Esquerdo", "em -0.6");
            }
            //CONTROLE DO MOTOR DE TRAÇÃO ESQUERDO EM +0.6
            else if (gamepad1.left_bumper) {
                motor_esq.setPower(0.6);
                telemetry.addData("Motor Esquerdo", "em +0.6");
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
            motor_sh.setPower(0.63);
            telemetry.addData("Shooter", "ativo");
        }
        else if (gamepad2.left_bumper) {
            motor_sh.setPower(-0.07);
            telemetry.addData("Shooter", "parando");
        }
        else {
            motor_sh.setPower(0);
        }

        //CONFIGURAÇÃO DO INTAKE (SERVOS E MOTOR)
        if (gamepad2.a) {
            servoit_esq.setPower(1.0);
            servoit_dir.setPower(1.0);
            motor_it.setPower(1.0);
            telemetry.addData("Intake", "ativo!");
        }
        else if (gamepad2.b) {
            motor_it.setPower(-1.0);
            telemetry.addData("Intake", "negativamente ativo");
        }
        else {
            servoit_esq.setPower(0);
            servoit_dir.setPower(0);
            motor_it.setPower(0);
        }

        //CONFIGURANDO O SERVO DO LIFT
        if (gamepad2.x) {
            servo_lift.setPower(1.0);
            telemetry.addData("Catraca", "em sentido HORÁRIO");
        }
        else if (gamepad2.y) {
            servo_catraca.setPosition(-1.0);
            telemetry.addData("Catraca", "em sentido ANTI-HORÁRIO");
        }
        else {
            servo_catraca.setPosition(0);
        }

        //Mensagem de atualização
        telemetry.update();
    }
}