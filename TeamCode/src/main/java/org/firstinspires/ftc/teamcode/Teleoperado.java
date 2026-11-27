package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "Teleoperado")
public class Teleoperado extends OpMode {

    //---------------------
    //NOMEANDO AS VÁRIAVEIS
    //---------------------

    //MOTORES
    public DcMotor motor_dir; //Porta de motor 0 (312 RPM)
    public DcMotor motor_esq; //Porta de motor 1 (312 RPM)
    public DcMotor motor_it; //Porta de motor 2 (+/- 4 000 RPM)
    public DcMotor motor_sh; //Porta de motor 3 (6 000 RPM)

    //SERVOS
    public CRServo servo_lift; //Porta de servos 0 (Speed)
    public Servo servo_catraca; //Porta de servo 1 (Speed)
    public CRServo servo_flw; //Porta de servo 2 (Torque)



    //TEMPORIZADOR DO LIFT +
    public ElapsedTime subir_lift = new ElapsedTime();
    public boolean lift_subindo = false;
    public double tempop_lift = 46.7; //TEMPO DE ATIVAÇÃO

    //TEMPORIZADOR DO LIFT -
    public ElapsedTime descer_lift = new ElapsedTime();
    public boolean lift_descendo = false;
    public double tempon_lift = 46.7; //TEMPO DE ATIVAÇÃO




    //TEMPORIZADOR DA CATRACA PARA A FLOWER
    public ElapsedTime abrir_flower = new ElapsedTime();
    public boolean flower_abrindo = false;
    public double tempo_abrir_flower = 0.4; //TEMPO DE ATIVAÇÃO

    //TEMPORIZADOR DA CATRACA PARA O SHOOTER
    public ElapsedTime abrir_shooter = new ElapsedTime();
    public boolean shooter_abrindo = false;
    public double tempo_abrir_shooter = 0.55; //TEMPO DE ATIVAÇÃO


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
        servo_lift = hardwareMap.get(CRServo.class, "servo_lift");
        servo_catraca = hardwareMap.get(Servo.class, "servo_catraca");
        servo_flw = hardwareMap.get(CRServo.class, "servo_flw");

        //EXPANDINDO O RANGE PWM PARA OS SERVOS
        ((CRServoImplEx) servo_lift).setPwmRange(new PwmControl.PwmRange(500, 2500));
        ((ServoImplEx) servo_catraca).setPwmRange(new PwmControl.PwmRange(500, 2500));
        ((CRServoImplEx) servo_flw).setPwmRange(new PwmControl.PwmRange(500, 2500));

        //INVERTENDO SENTIDO DOS SERVOS
        servo_lift.setDirection(CRServo.Direction.REVERSE);
        servo_flw.setDirection(CRServo.Direction.REVERSE);

        //MENSAGEM DE INICIALIZAÇÃO
        telemetry.addData("Vibranium", "pronto para arrasar!");
        telemetry.update();
    }


    @Override
    public void loop() {

        // ------------------------
        // GAMEPAD 1 - MOVIMENTAÇÃO
        // ------------------------

        //-----------------------------------------
        //CONFIGURAÇÃO DOS MOTORES DE TRAÇÃO JUNTOS
        //-----------------------------------------

        //CONTROLE DOS MOTORES DE TRAÇÃO POSITIVAMENTE
        if (gamepad1.a) {
            motor_dir.setPower(0.85);
            motor_esq.setPower(0.85);
            telemetry.addData("Vibranium", "tá voando!");
        }
        //CONTROLE DOS MOTORES DE TRAÇÃO NEGATIVAMENTE
        else if (gamepad1.y) {
            motor_dir.setPower(-0.85);
            motor_esq.setPower(-0.85);
            telemetry.addData("Vibranium", "tá dando ré");
        }

        //---------------------------------------------
        //CONFIGURAÇÃO INDIVIDUAL DOS MOTORES DE TRAÇÃO
        //---------------------------------------------
        else {

            //-------------
            //MOTOR DIREITO
            //-------------

            //CONTROLE DO MOTOR DE TRAÇÃO DIREITO EM -0.7
            if (gamepad1.b && gamepad1.right_bumper) {
                motor_dir.setPower(-0.7);
                telemetry.addData("Motor Direito", "tá devagarzinho pra trás");
            }
            //CONTROLE DO MOTOR DE TRAÇÃO DIREITO EM +0.7
            else if (gamepad1.right_bumper) {
                motor_dir.setPower(0.7);
                telemetry.addData("Motor Direito", "tá devagarzinho pra frente");
            }
            //CONTROLE DO MOTOR DE TRAÇÃO DIREITO NO ANALÓGICO
            else {
                double potencia_dir = -gamepad1.right_stick_y;
                motor_dir.setPower(potencia_dir);
                telemetry.addData("Potência Direita", potencia_dir);
            }

            //--------------
            //MOTOR ESQUERDO
            //--------------

            //CONTROLE DO MOTOR DE TRAÇÃO ESQUERDO EM -0.7
            if (gamepad1.b && gamepad1.left_bumper) {
                motor_esq.setPower(-0.7);
                telemetry.addData("Motor Esquerdo", "tá devagarzinho pra trás");
            }
            //CONTROLE DO MOTOR DE TRAÇÃO ESQUERDO EM +0.7
            else if (gamepad1.left_bumper) {
                motor_esq.setPower(0.7);
                telemetry.addData("Motor Esquerdo", "tá devagarzinho pra frente");
            }
            //CONTROLE DO MOTOR DE TRAÇÃO ESQUERDO NO ANALÓGICO
            else {
                double potencia_esq = -gamepad1.left_stick_y;
                motor_esq.setPower(potencia_esq);
                telemetry.addData("Potência Esquerda", potencia_esq);
            }

        }


        // --------------------
        // GAMEPAD 2 - SISTEMAS
        // --------------------


        //CONTROLE DO SHOOTER
        if (gamepad2.right_bumper) {
            motor_sh.setPower(0.78);
            telemetry.addData("Shooter", "em 78%");
        } else if (gamepad2.left_bumper) {
            motor_sh.setPower(-0.07);
            telemetry.addData("Shooter", "parando");
        } else {
            motor_sh.setPower(0);
        }

        //=================================================================================================================================
        //CONTROLE DO LIFT -
        if (gamepad2.dpad_down && !lift_descendo) {
            descer_lift.reset();
            lift_descendo = true;
            lift_subindo = false;
            servo_lift.setPower(-1.0);
        }
        //VERIFICAÇÃO DE TEMPO
        if (lift_descendo && descer_lift.seconds() >= tempon_lift) {
            servo_lift.setPower(0.0);
            lift_descendo = false;
        }
        if (lift_descendo){
            telemetry.addData("Lift", "descendo (tempo: " + String.format("%.1f", descer_lift.seconds()) + "s)");
        }
        //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            //CONTROLE DO LIFT +
        if (gamepad2.dpad_up && !lift_subindo) {
            subir_lift.reset();
            lift_subindo = true;
            lift_descendo = false;
            servo_lift.setPower(1.0);
        }
        //VERIFICAÇÃO DE TEMPO
        if (lift_subindo && subir_lift.seconds() >= tempop_lift) {
            servo_lift.setPower(0.0);
            lift_subindo = false;
        }
        if (lift_subindo){
            telemetry.addData("Lift", "subindo (tempo: " + String.format("%.1f", subir_lift.seconds()) + "s)");
        }

        //PARANDO O LIFT
        if (gamepad2.x) {
            lift_descendo = false;
            lift_subindo = false;
            servo_lift.setPower(0.0);
        }
        //====================================================================================================================================

        //====================================================================================================================================
        //PROGRAMANDO A PORTA DO ARMAZENADOR
        if (gamepad1.dpad_up) {
            servo_catraca.setPosition(0.42); //Em pé
        }
        else if (gamepad1.dpad_right) {
            servo_catraca.setPosition(0.75); //Deitado
        }
        //====================================================================================================================================

        //CONFIGURAÇÃO DO INTAKE
        if (gamepad2.a) {
            motor_it.setPower(1.0);

            telemetry.addData("Intake", "sugando tudo!");
        } else if (gamepad2.b) {
            motor_it.setPower(-1.0);

            telemetry.addData("Intake", "cuspindo as esferas?!");
        } else {
            motor_it.setPower(0);
        }

        //ATUALIZANDO TODAS AS TELEMETRIAS
        telemetry.update();
    }
}