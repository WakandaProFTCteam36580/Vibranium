package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.PwmControl;

@TeleOp(name = "TesteAlone")

public class TesteAlone extends OpMode {

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

        //EXPANDINDO O RANGE PWM PARA OS SERVOS
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

        //CONFIGURAÇÕES PARA CONTROLAR O SHOOTER
        if (gamepad1.right_trigger > 0.1) {
            motor_sh.setPower(0.65);
        }
        else if (gamepad1.left_trigger > 0.1) {
            motor_sh.setPower(-0.07);
        }
        else {
            motor_sh.setPower(0);
        }

        //CONTROLE DO INTAKE E SERVOS COM RB E LB
        //POSITIVO
        if (gamepad1.right_bumper) {
            motor_it.setPower(1.0);
            servoit_dir.setPower(1.0);
            servoit_esq.setPower(1.0);
            telemetry.addData("Intake", "GIRANDO POSITIVO (+1.0)");
        }
        //NEGATIVO
        else if (gamepad1.left_bumper) {
            motor_it.setPower(-1.0);
            servoit_dir.setPower(0);
            servoit_esq.setPower(0);
            telemetry.addData("Intake", "GIRANDO NEGATIVO (-1.0)");
        }
        else {
            // DESLIGANDO
            motor_it.setPower(0);
            servoit_esq.setPower(0);
            servoit_dir.setPower(0);
            telemetry.addData("Intake", "PARADO");
        }

        //CONFIGURAÇÃO DOS MOTORES DE TRAÇÃO JUNTOS
        if (gamepad1.a) {
            motor_dir.setPower(0.9);
            motor_esq.setPower(0.9);
            telemetry.addData("Tração", "Botão A (+0.9)");
        }
        else if (gamepad1.y) {
            motor_dir.setPower(-0.9);
            motor_esq.setPower(-0.9);
            telemetry.addData("Tração", "Botão Y (-0.9)");
        }
        else {
            //CONFIGURAÇÃO INDIVIDUAL DOS MOTORES DE TRAÇÃO NOS ANALÓGICOS
            double potencia_dir = -gamepad1.right_stick_y;
            motor_dir.setPower(potencia_dir);

            double potencia_esq = -gamepad1.left_stick_y;
            motor_esq.setPower(potencia_esq);

            telemetry.addData("Tração", "Analógicos ativos");
        }

        //Mensagem de atualização da telemetria (para você ler os status na tela)
        telemetry.update();
    }
}