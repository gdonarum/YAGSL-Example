package frc.robot.subsystems;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.LEDConstants;
import java.util.function.BooleanSupplier;

public class LEDSubsystem extends SubsystemBase
{

  private final AddressableLED led;
  private final AddressableLEDBuffer ledBuffer;
  private final BooleanSupplier visionWorking;

  /**
   * Creates a new LEDSubsystem.
   *
   * @param visionWorking Supplier that returns true when vision odometry is providing good data.
   */
  public LEDSubsystem(BooleanSupplier visionWorking)
  {
    this.visionWorking = visionWorking;

    led = new AddressableLED(LEDConstants.LED_PWM_PORT);
    ledBuffer = new AddressableLEDBuffer(LEDConstants.LED_LENGTH);
    led.setLength(ledBuffer.getLength());
    led.setData(ledBuffer);
    led.start();
  }

  @Override
  public void periodic()
  {
    if (visionWorking.getAsBoolean())
    {
      setAllColor(0, 255, 0); // Green when vision odometry is good
    } else
    {
      setAllianceColor();
    }

    led.setData(ledBuffer);
  }

  /**
   * Sets the entire LED strip to the current alliance color. Defaults to red if alliance data is unavailable.
   */
  private void setAllianceColor()
  {
    var alliance = DriverStation.getAlliance();
    if (alliance.isPresent() && alliance.get() == Alliance.Blue)
    {
      setAllColor(0, 0, 255);
    } else
    {
      setAllColor(255, 0, 0);
    }
  }

  /**
   * Sets every LED in the strip to the specified RGB color.
   *
   * @param r Red value (0-255).
   * @param g Green value (0-255).
   * @param b Blue value (0-255).
   */
  private void setAllColor(int r, int g, int b)
  {
    for (int i = 0; i < ledBuffer.getLength(); i++)
    {
      ledBuffer.setRGB(i, r, g, b);
    }
  }
}
