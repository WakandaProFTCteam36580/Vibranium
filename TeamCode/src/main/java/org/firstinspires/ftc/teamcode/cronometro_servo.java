package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "Cronometro Lift")
public class cronometro_servo extends OpMode {

    //---------------------
    //NOMEANDO AS VÁRIAVEIS
    //---------------------

    //SERVO
    public CRServo servo_lift;

    //TEMPORIZADOR DO LIFT
    public ElapsedTime timer = new ElapsedTime();
    public boolean servo_rodando = false;
    public double tempo_limite = 7.0; //TEMPO DE ATIVAÇÃO

    @Override
    public void init() {
        // Mapeamento do hardware usando "servo_lift"
        servo_lift = hardwareMap.get(CRServo.class, "servo_lift");

        telemetry.addData("Teste dos Servos", "Pronto!");
        telemetry.update();
    }

    @Override
    public void loop() {

        if (gamepad1.a && !servo_rodando) {
            timer.reset();               // Zera o cronômetro para começar a contar do zero
            servo_rodando = true;        // Avisa ao programa que o servo está em movimento
            servo_lift.setPower(1.0);    // Liga o servo na força máxima
        }

        // Verifica se o servo está rodando e se o cronômetro já atingiu 7 segundos
        if (servo_rodando && timer.seconds() >= tempo_limite) {
            servo_lift.setPower(0.0);    // Desliga o servo
            servo_rodando = false;       // Reseta o estado para poder apertar 'A' de novo
        }

        // Telemetria para acompanhar o tempo na tela do Driver Station
        if (servo_rodando) {
            telemetry.addData("Status do Servo", "RODANDO");
            telemetry.addData("Tempo passado", String.format("%.1f", timer.seconds()) + " / 7.0 segundos");
        } else {
            telemetry.addData("Status do Servo", "PARADO");
            telemetry.addData("Instrução", "Aperte 'A' para iniciar novamente");
        }

        telemetry.update();
    }
}