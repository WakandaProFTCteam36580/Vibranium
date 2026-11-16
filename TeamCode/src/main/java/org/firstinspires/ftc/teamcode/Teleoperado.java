package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;

@TeleOp(name = "Teleoperado")

public class Teleoperado extends OpMode {

    //Nomeando as variáveis
    public DcMotor motor_dir; //Porta de motor 0 (312 RPM)
    public DcMotor motor_esq; //Porta de motor 1 (312 RPM)
    public DcMotor motor_it; //Porta de motor 2 (117 RPM)
    public DcMotor motor_sh; //Porta de motor 3 (6 000 RPM)

    public Servo servo_lift; //Porta de servos 0 (Speed)
    public Servo servo_catraca; //Porta de servo 1 (Speed)
    public Servo servo_flw; //Porta de servo 2 (Torque)


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
        servo_lift = hardwareMap.get(Servo.class, "servo_lift");
        servo_catraca = hardwareMap.get(Servo.class, "servo_catraca");
        servo_flw = hardwareMap.get(Servo.class,"servo_flw");

        //EXPANDINDO O RANGE PWM PARA OS SERVOS SERVOS
        ((ServoImplEx) servo_lift).setPwmRange (new PwmControl.PwmRange(500,2500));
        ((ServoImplEx)servo_catraca).setPwmRange (new PwmControl.PwmRange (500,2500));
        ((ServoImplEx)servo_flw).setPwmRange (new PwmControl.PwmRange (500,2500));

        //INVERTENDO SENTIDO DOS SERVOS
        servo_lift.setDirection(Servo.Direction.REVERSE);
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
            motor_it.setPower(1.0);
            telemetry.addData("Intake", "ativo!");
        }
        else if (gamepad2.b) {
            motor_it.setPower(-1.0);
            telemetry.addData("Intake", "negativamente ativo");
        }
        else {
            motor_it.setPower(0);
        }


        //Mensagem de atualização
        telemetry.update();
    }
}