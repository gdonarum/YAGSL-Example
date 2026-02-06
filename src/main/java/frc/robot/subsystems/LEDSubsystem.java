package frc.robot.subsystems;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.LEDConstants;
import org.photonvision.PhotonCamera;

public class LEDSubsystem extends SubsystemBase
{

  private final AddressableLED led;
  private final AddressableLEDBuffer ledBuffer;
  private final PhotonCamera camera;

  /**
   * Creates a new LEDSubsystem.
   *
   * @param camera The PhotonVision camera used for vision-based odometry.
   */
  public LEDSubsystem(PhotonCamera camera)
  {
    this.camera = camera;

    led = new AddressableLED(LEDConstants.LED_PWM_PORT);
    ledBuffer = new AddressableLEDBuffer(LEDConstants.LED_LENGTH);
    led.setLength(ledBuffer.getLength());
    led.setData(ledBuffer);
    led.start();
  }

  @Override
  public void periodic()
  {
    if (hasGoodVisionOdometry())
    {
      setAllColor(0, 255, 0); // Green when vision odometry is good
    } else
    {
      setAllianceColor();
    }

    led.setData(ledBuffer);
  }

  /**
   * Checks whether PhotonVision is currently providing good odometry data. This requires the camera to see at least one
   * AprilTag target with low pose ambiguity.
   *
   * @return true if vision odometry is reliable.
   */
  private boolean hasGoodVisionOdometry()
  {
    var result = camera.getLatestResult();
    if (!result.hasTargets())
    {
      return false;
    }
    return result.getBestTarget().getPoseAmbiguity() < LEDConstants.AMBIGUITY_THRESHOLD
           && result.getBestTarget().getPoseAmbiguity() >= 0;
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
