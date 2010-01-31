package autobot;

import com.google.wave.api.ProfileServlet;

public class Auto_BotProfileServlet extends ProfileServlet {
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
    return "http://i.imgur.com/CQoTo.png";
  }
 
  @Override
  public String getRobotProfilePageUrl() {
    return "auto-bot.appspot.com";
  }
}