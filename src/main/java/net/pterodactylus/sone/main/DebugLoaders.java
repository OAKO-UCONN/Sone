package net.pterodactylus.sone.main;

import java.io.File;
import javax.annotation.Nonnull;

import net.pterodactylus.sone.template.FilesystemTemplate;
import net.pterodactylus.sone.web.pages.ReloadingPage;
import net.pterodactylus.util.template.FilesystemTemplateProvider;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateProvider;
import net.pterodactylus.util.web.Page;
import net.pterodactylus.util.web.Request;

/**
 * {@link Loaders} implementation that loads all resources from the filesystem.
 */
public class DebugLoaders implements Loaders {

	private final String filesystemPath;

	public DebugLoaders(String filesystemPath) {
		this.filesystemPath = filesystemPath;
	}

	@Nonnull
	@Override
	public Template loadTemplate(@Nonnull String path) {
		return new FilesystemTemplate(new File(filesystemPath, path).getAbsolutePath());
	}

	@Nonnull
	@Override
	public <REQ extends Request> Page<REQ> loadStaticPage(@Nonnull String basePath, @Nonnull String prefix, @Nonnull String mimeType) {
		return new ReloadingPage<>(basePath, new File(filesystemPath, prefix).getAbsolutePath(), mimeType);
	}

	@Nonnull
	@Override
	public TemplateProvider getTemplateProvider() {
		return new FilesystemTemplateProvider(new File(filesystemPath, "/templates/").getAbsolutePath());
	}

}
