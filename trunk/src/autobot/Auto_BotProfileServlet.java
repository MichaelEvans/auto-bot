package autobot;

import com.google.wave.api.ProfileServlet;

public class Auto_BotProfileServlet extends ProfileServlet {
  @Override
  public String getRobotName() {
    return "Auto-Bot";
  }
 
  @Override
  public String getRobotAvatarUrl() {
    return "http://imgur.com/CQoTo.png";
  }
 
  @Override
  public String getRobotProfilePageUrl() {
    return "auto-bot.appspot.com";
  }
}