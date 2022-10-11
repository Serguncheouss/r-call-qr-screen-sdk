# SDK for R-Call QR screens.
## Usage:
```
try (var screen = new Screen("COM1")) {
  screen.setReadTimeout(3000);

  System.out.println("Version: " + screen.getVersion());
  System.out.println("Generated ID: " + screen.getId());

  if (!screen.showHeader("R-Call")) {
    System.out.println("Unable to show the header.");
  }
  if (!screen.showQrWithLogo("https://github.com/Serguncheouss/r-call-qr-screen-sdk")) {
    System.out.println("Unable to show the QR code.");
  }
  if (!screen.showFooter("QR-Screen")) {
    System.out.println("Unable to show the footer.");
  }

  Thread.sleep(2000);
  screen.clear();
  Thread.sleep(2000);
  screen.switchOff();
} catch (InterruptedException ignored) {
} catch (IOException e) {
  e.printStackTrace();
}
```
