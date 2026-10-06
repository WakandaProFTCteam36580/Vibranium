package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

//nomenando classe como teleoperada
@TeleOp(name = "TeleOpTeste")

public class Teste_motores extends OpMode{

    //nomeando a variável
    DcMotor motor_dir;
    DcMotor motor_esq;

    @Override
    public void init() {

        //Importanto a variável para o programa.
        motor_dir = hardwareMap.get(DcMotor.class, "motor_dir");
        motor_esq = hardwareMap.get(DcMotor.class, "motor_esq");

        motor_esq.setDirection(DcMotor.Direction.REVERSE);
        //adicionando mensagem que aparecerá no driver hub
        telemetry.addData("Motores", "Rodando código");
        telemetry.update();

    }

    @Override
    public void loop() {

        //adicionando uma variável float (serve para valores variáveis)
        double potencia = gamepad1.right_stick_y;

            motor_dir.setPower(potencia);
            motor_esq.setPower(potencia);

            telemetry.addData("Potência aplicada ao motor",potencia);
            telemetry.update();

        }

    }