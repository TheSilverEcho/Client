package theSilverEcho.tweaks.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashScreen;
import net.minecraft.resource.ResourceReloadMonitor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import theSilverEcho.tweaks.Tweaks;
import theSilverEcho.tweaks.gui.GuiHelper;
import theSilverEcho.tweaks.notification.Notification;
import theSilverEcho.tweaks.notification.NotificationManager;

import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(SplashScreen.class) public abstract class SplashScreenMixin extends Overlay
{

	private static final Identifier LOGO = new Identifier("tweaks", "textures/ui/splash.png");
	@Shadow
	@Final
	private ResourceReloadMonitor reloadMonitor;
	@Shadow
	@Final
	private Consumer<Optional<Throwable>> exceptionHandler;
	@Shadow
	@Final
	private boolean reloading;
	@Shadow private float progress;
	@Shadow private long prepareCompleteTime;

	//	@Shadow protected abstract void renderProgressBar(int minX, int minY, int maxX, int maxY, float progress);

	@Shadow private long applyCompleteTime;

	@Inject(method = "render", at = @At(value = "HEAD"), cancellable = true) public void init(int mouseX, int mouseY, float delta, CallbackInfo ci)
	{
		ci.cancel();
		int i = MinecraftClient.getInstance().getWindow().getScaledWidth();
		int j = MinecraftClient.getInstance().getWindow().getScaledHeight();
		long l = Util.getMeasuringTimeMs();
		if (this.reloading && (this.reloadMonitor.isPrepareStageComplete() || MinecraftClient
				.getInstance().currentScreen != null) && this.prepareCompleteTime == -1L)
		{
			this.prepareCompleteTime = l;
		}

		float f = this.applyCompleteTime > -1L ? (float) (l - this.applyCompleteTime) / 1000.0F : -1.0F;
		float g = this.prepareCompleteTime > -1L ? (float) (l - this.prepareCompleteTime) / 500.0F : -1.0F;
		float o;
		int m;
		if (f >= 1.0F)
		{
			if (MinecraftClient.getInstance().currentScreen != null)
			{
				MinecraftClient.getInstance().currentScreen.render(0, 0, delta);
			}

			m = MathHelper.ceil((1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
			GuiHelper.fill(7, 0, 0, i, j, 16777215 | m << 24);
			o = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
		} else if (this.reloading)
		{
			if (MinecraftClient.getInstance().currentScreen != null && g < 1.0F)
			{
				MinecraftClient.getInstance().currentScreen.render(mouseX, mouseY, delta);
			}

			m = MathHelper.ceil(MathHelper.clamp(g, 0.15D, 1.0D) * 255.0D);
			GuiHelper.fill(0, 0, i, j, 16777215 | m << 24);
			o = MathHelper.clamp(g, 0.0F, 1.0F);
		} else
		{
			GuiHelper.fill(0, 0, i, j, -1);
			o = 1.0F;
		}

		MinecraftClient.getInstance().getTextureManager().bindTexture(LOGO);
		RenderSystem.enableBlend();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, o);
		blit(0, 0, MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight(), 0.0F, 0.0F, 16, 128, 16, 128);
		//		blit(0, 0, 0, 0, MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight());
		GuiHelper.fillGradient(7, 0, 0, MinecraftClient.getInstance().getWindow().getScaledWidth(),
				MinecraftClient.getInstance().getWindow().getScaledHeight(), new Color(46, 52, 64, 35).getRGB(), new Color(76, 86, 106).getRGB());
		float r = this.reloadMonitor.getProgress();
		this.progress = MathHelper.clamp(this.progress * 0.95F + r * 0.050000012F, 0.0F, 1.0F);
		if (f < 1.0F)
		{

			//			fill(i / 2 - 150, 0, i / 2 + 150, 21, -1);
			this.renderProgressBar(i / 2 - 150, j / 4 * 3, i / 2 + 150, j / 4 * 3 + 12);
			String text = "Progress: " + progress * 100 + "%";
			Tweaks.renderer.drawString(text, (float) (i / 2 - Tweaks.renderer.getStringWidth(text) / 2), (float) j / 4 * 3 - 1, -1, false, 0.4F);
		}

		if (f >= 2.0F)
		{
			MinecraftClient.getInstance().setOverlay(null);
		}

		if (this.applyCompleteTime == -1L && this.reloadMonitor.isApplyStageComplete() && (!this.reloading || g >= 2.0F))
		{
			System.out.println(Util.getMeasuringTimeMs() - applyCompleteTime);
			NotificationManager.show(
					new Notification("Time taken to load", Math.round((Util.getMeasuringTimeMs() - applyCompleteTime) / 1000F) + " seconds", 30));

			try
			{
				this.reloadMonitor.throwExceptions();
				this.exceptionHandler.accept(Optional.empty());
			} catch (Throwable var15)
			{
				this.exceptionHandler.accept(Optional.of(var15));
			}

			this.applyCompleteTime = Util.getMeasuringTimeMs();
			if (MinecraftClient.getInstance().currentScreen != null)
			{
				MinecraftClient.getInstance().currentScreen.init(MinecraftClient.getInstance(),
						MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight());
			}
		}

	}

	private void renderProgressBar(int minX, int minY, int maxX, int maxY)
	{
		int i = MathHelper.ceil((float) (maxX - minX - 1) * this.progress);
		//		GuiHelper.fillEllipse(GL11.GL_TRIANGLE_FAN, 20, 20, 10, 10, -1, (int) (360 * this.progress));

		fill(minX - 1, minY - 1, maxX + 1, maxY + 1, new Color(76, 86, 106).getRGB());
		fill(minX, minY, maxX, maxY, new Color(70, 77, 93).getRGB());//inner colour
		fill(minX + 1, minY + 1, minX + i, maxY - 1, new Color(143, 188, 187).getRGB() /*| 143 << 16 | 188 << 8 | 187*/);
	}
}
