/*
 * package mahjong_package;
 * 
 * import java.io.IOException; import java.nio.charset.StandardCharsets; import
 * java.nio.file.Files; import java.nio.file.Path; import java.nio.file.Paths;
 * import java.util.Arrays; import java.util.List;
 * 
 * 
 * //FIXME: code these exceptions below
 * 
 * public class Mailbox {
 * 
 * private final String gamelog = "./gamelog.txt"; private final String applog =
 * "./applog.txt"; private Path gamePath = Paths.get(gamelog); private Path
 * appPath = Paths.get(applog); private MailboxAccess access;
 * 
 * // constructor for mail public Mailbox() { // constructor no arguments
 * this.access = MailboxAccess.NONE; }
 * 
 * 
 * // write mail in game public boolean writeGameMail(String text) throws
 * IOException { boolean success = false; List<String> lines =
 * Arrays.asList(text); if (this.checkAccess() == MailboxAccess.GAME) { try {
 * Files.write(gamePath, lines, StandardCharsets.UTF_8); success = true; } catch
 * (IOException e) { System.err.println("Caught IOException msg: " +
 * e.getMessage()); } finally { // } } return success; }
 * 
 * 
 * // write mail in app public boolean writeAppMail(String text) throws
 * IOException { boolean success = false; List<String> lines =
 * Arrays.asList(text); if (this.checkAccess() == MailboxAccess.APP) { try {
 * Files.write(appPath, lines, StandardCharsets.UTF_8); success = true; } catch
 * (IOException e) { System.err.println("Caught IOException msg: " +
 * e.getMessage()); } finally { // } } return success; }
 * 
 * 
 * // get mail and empty mail public String readGameMail() throws IOException {
 * String msg = new String(""); if (this.checkAccess() == MailboxAccess.GAME) {
 * try { msg = new String(Files.readAllBytes(Paths.get(this.gamelog))); } catch
 * (IOException e) {
 * System.err.println("Caught IOException msg when reading Game mail: " +
 * e.getMessage()); } finally { // } } return msg; }
 * 
 * 
 * // get mail and empty mail public String readAppMail() throws IOException {
 * String msg = new String(""); if (this.checkAccess() == MailboxAccess.APP) {
 * try { msg = new String(Files.readAllBytes(Paths.get(this.applog))); } catch
 * (IOException e) {
 * System.err.println("Caught IOException msg when reading App mail: " +
 * e.getMessage()); } finally { // } } return msg; }
 * 
 * 
 * // set access to Android app user public void setAppAccess() { this.access =
 * MailboxAccess.APP; }
 * 
 * 
 * // set access to Game public void setGameAccess() { this.access =
 * MailboxAccess.GAME; }
 * 
 * 
 * // check access public MailboxAccess checkAccess() { return this.access; }
 * 
 * }
 */