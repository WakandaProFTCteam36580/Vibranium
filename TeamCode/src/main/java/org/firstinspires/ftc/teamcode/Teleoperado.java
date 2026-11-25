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
    public DcMotor motor_it; //Porta de motor 2 (117 RPM)
    public DcMotor motor_sh; //Porta de motor 3 (6 000 RPM)

    //SERVOS
    public CRServo servo_lift; //Porta de servos 0 (Speed)
    public Servo servo_catraca; //Porta de servo 1 (Speed)
    public Servo servo_flw; //Porta de servo 2 (Torque)

    //TEMPORIZADOR DO LIFT +
    public ElapsedTime subir_lift = new ElapsedTime();
    public boolean lift_subindo = false;
    public double tempop_lift = 46.7; //TEMPO DE ATIVAÇÃO

    //TEMPORIZADOR DO LIFT -
    public ElapsedTime descer_lift = new ElapsedTime();
    public boolean lift_descendo = false;
    public double tempon_lift = 46.7; //TEMPO DE ATIVAÇÃO




    //TEMPORIZADOR DO LIFT
    public ElapsedTime timer_lift = new ElapsedTime();
    public boolean lift_rodando = false;
    public double tempo_lift = 2.0; //TEMPO DE ATIVAÇÃO


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
        servo_flw = hardwareMap.get(Servo.class,"servo_flw");

        //EXPANDINDO O RANGE PWM PARA OS SERVOS
        ((CRServoImplEx)servo_lift).setPwmRange (new PwmControl.PwmRange(500,2500));
        ((ServoImplEx)servo_catraca).setPwmRange (new PwmControl.PwmRange (500,2500));
        ((ServoImplEx)servo_flw).setPwmRange (new PwmControl.PwmRange (500,2500));

        //INVERTENDO SENTIDO DOS SERVOS
        servo_lift.setDirection(CRServo.Direction.REVERSE);
        servo_catraca.setDirection(Servo.Direction.REVERSE);

        //MENSAGEM DE INICIALIZAÇÃO
        telemetry.addData("Modo teleoperado", "ativo!");
        telemetry.update();
    }



        @Override
        public void loop() {

        // ------------------------
        // GAMEPAD 1 - MOVIMENTAÇÃO
        // ------------------------

        //CONFIGURAÇÃO DOS MOTORES DE TRAÇÃO JUNTOS
        //CONTROLE DOS MOTORES DE TRAÇÃO POSITIVAMENTE
        if (gamepad1.a) {
            motor_dir.setPower(1.0);
            motor_esq.setPower(1.0);
            telemetry.addData("Vibranium", "tá voando!");
        }
        //CONTROLE DOS MOTORES DE TRAÇÃO NEGATIVAMENTE
        else if (gamepad1.y) {
            motor_dir.setPower(-1.0);
            motor_esq.setPower(-1.0);
            telemetry.addData("Vibranium", "tá dando ré");
        }

        //CONFIGURAÇÃO INDIVIDUAL DOS MOTORES DE TRAÇÃO
        else {

            //CONTROLE DO MOTOR DE TRAÇÃO DIREITO EM -0.6
            if (gamepad1.b && gamepad1.right_bumper) {
                motor_dir.setPower(-0.6);
                telemetry.addData("Motor Direito", "tá devagarzinho pra trás");
            }
            //CONTROLE DO MOTOR DE TRAÇÃO DIREITO EM +0.6
            else if (gamepad1.right_bumper) {
                motor_dir.setPower(0.6);
                telemetry.addData("Motor Direito", "tá devagarzinho pra frente");
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
                telemetry.addData("Motor Esquerdo", "tá devagarzinho pra trás");
            }
            //CONTROLE DO MOTOR DE TRAÇÃO ESQUERDO EM +0.6
            else if (gamepad1.left_bumper) {
                motor_esq.setPower(0.6);
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
        //TELEMETRIA
        if (lift_descendo) {
            telemetry.addData("Lift", "descendo (tempo: " + String.format("%.1f", descer_lift.seconds()) + "s)");
        } else {
            telemetry.addData("Lift", "descansando");
        }

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
        //TELEMETRIA
        if (lift_subindo) {
            telemetry.addData("Lift", "subindo (tempo: " + String.format("%.1f", subir_lift.seconds()) + "s)");
        } else {
            telemetry.addData("Lift", "descansando");
        }


        //CONFIGURAÇÃO DO INTAKE
        if (gamepad2.a) {
            motor_it.setPower(1.0);
            telemetry.addData("Intake", "sugando tudo!");
        }
        else if (gamepad2.b) {
            motor_it.setPower(-1.0);
            telemetry.addData("Intake", "cuspindo as esferas?!");
        }
        else {
            motor_it.setPower(0);
        }

        // CONTROLE DO LIFT TEMPORIZADO (CRSERVO) - ADICIONADO
        if (gamepad2.x && !lift_rodando) {
            timer_lift.reset();
            lift_rodando = true;
            servo_lift.setPower(1.0);
        }

        // Verifica se o tempo limite já passou
        if (lift_rodando && timer_lift.seconds() >= tempo_lift) {
            servo_lift.setPower(0.0);
            lift_rodando = false;
        }

        // Telemetria do Lift
        if (lift_rodando) {
            telemetry.addData("Lift", "Rodando (tempo: " + String.format("%.1f", timer_lift.seconds()) + "s)");
        } else {
            telemetry.addData("Lift", "Parado");
        }


        //Mensagem de atualização
        telemetry.update();
    }
}