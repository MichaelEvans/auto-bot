package autobot;

import com.google.wave.api.*;

public class Auto_BotProfileServlet extends AbstractRobot {
  /**
	 * 
	 */
	private static final long serialVersionUID = -2169503921810859424L;

@Override
  public String getRobotName() {
    return "Auto-Bot";
  }
 
  @Override
  public String getRobotAvatarUrl() {
	  return "http://auto-bot.appspot.com/_wave/autobot.png";
  }
 
  @Override
  public String getRobotProfilePageUrl() {
    return "auto-bot.appspot.com";
  }
}