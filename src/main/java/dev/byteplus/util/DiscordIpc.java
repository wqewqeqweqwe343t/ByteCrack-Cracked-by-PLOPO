package dev.byteplus.util;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class DiscordIpc implements AutoCloseable {
   private RandomAccessFile pipe;
   private SocketChannel socket;

   public static DiscordIpc connect(String clientId) throws IOException {
      DiscordIpc ipc = new DiscordIpc();
      ipc.open();
      JsonObject hello = new JsonObject();
      hello.addProperty("v", 1);
      hello.addProperty("client_id", clientId);
      ipc.send(0, hello);
      ipc.read();
      return ipc;
   }

   private void open() throws IOException {
      boolean windows = System.getProperty("os.name").toLowerCase().contains("win");

      for (int i = 0; i < 10; i++) {
         try {
            if (windows) {
               this.pipe = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
               return;
            }

            for (String dir : new String[]{System.getenv("XDG_RUNTIME_DIR"), System.getenv("TMPDIR"), "/tmp"}) {
               if (dir != null) {
                  Path p = Path.of(dir, "discord-ipc-" + i);
                  if (Files.exists(p)) {
                     this.socket = SocketChannel.open(StandardProtocolFamily.UNIX);
                     this.socket.connect(UnixDomainSocketAddress.of(p));
                     return;
                  }
               }
            }
         } catch (IOException var8) {
         }
      }

      throw new IOException("Discord is not running");
   }

   public void setActivity(String details, String state, String largeImage, String largeText, long startEpochSeconds) throws IOException {
      JsonObject activity = new JsonObject();
      if (details != null && !details.isEmpty()) {
         activity.addProperty("details", details);
      }

      if (state != null && !state.isEmpty()) {
         activity.addProperty("state", state);
      }

      JsonObject ts = new JsonObject();
      ts.addProperty("start", startEpochSeconds);
      activity.add("timestamps", ts);
      JsonObject assets = new JsonObject();
      if (largeImage != null && !largeImage.isEmpty()) {
         assets.addProperty("large_image", largeImage);
      }

      if (largeText != null && !largeText.isEmpty()) {
         assets.addProperty("large_text", largeText);
      }

      activity.add("assets", assets);
      JsonObject args = new JsonObject();
      args.addProperty("pid", ProcessHandle.current().pid());
      args.add("activity", activity);
      JsonObject frame = new JsonObject();
      frame.addProperty("cmd", "SET_ACTIVITY");
      frame.add("args", args);
      frame.addProperty("nonce", UUID.randomUUID().toString());
      this.send(1, frame);
      this.read();
   }

   private void send(int op, JsonObject json) throws IOException {
      byte[] data = json.toString().getBytes(StandardCharsets.UTF_8);
      ByteBuffer buf = ByteBuffer.allocate(8 + data.length).order(ByteOrder.LITTLE_ENDIAN);
      buf.putInt(op).putInt(data.length).put(data).flip();
      if (this.pipe != null) {
         this.pipe.write(buf.array());
      } else {
         while (buf.hasRemaining()) {
            this.socket.write(buf);
         }
      }
   }

   private void read() throws IOException {
      byte[] header = new byte[8];
      this.readFully(header);
      int len = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN).getInt(4);
      if (len > 0 && len < 1048576) {
         this.readFully(new byte[len]);
      }
   }

   private void readFully(byte[] b) throws IOException {
      if (this.pipe != null) {
         this.pipe.readFully(b);
      } else {
         ByteBuffer buf = ByteBuffer.wrap(b);

         while (buf.hasRemaining()) {
            if (this.socket.read(buf) < 0) {
               throw new IOException("closed");
            }
         }
      }
   }

   @Override
   public void close() {
      try {
         if (this.pipe != null) {
            this.pipe.close();
         }
      } catch (IOException var3) {
      }

      try {
         if (this.socket != null) {
            this.socket.close();
         }
      } catch (IOException var2) {
      }
   }
}
