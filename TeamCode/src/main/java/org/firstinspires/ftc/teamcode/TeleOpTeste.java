package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

//nomenando classe como teleoperada
@TeleOp(name = "TeleOpTeste")

public class TeleOpTeste extends OpMode{

    //nomeando a variável
    DcMotor motor_dir;

    @Override
    public void init() {

        //importanto a variável para o programa
        motor_dir = hardwareMap.get(DcMotor.class, "motor_dir");

        //adicionando mensagem que aparecerá no driver hub
        telemetry.addData("Motor Direito", "Rodando código");
        telemetry.update();

    }

    @Override
    public void loop() {

        //adicionando uma variável float (serve para valores variáveis)
        float x = (gamepad1.right_stick_y);

        if (-x > 0){

            motor_dir.setPower(x);

        }

        else{

            //manda o motor parar quando a função do if não estiver sendo cumprida
            motor_dir.setPower(0);

        }

    }

}
