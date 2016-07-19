package ru.pnapp.simplerss;

import dagger.Component;

@Component(
        modules = UrlRssPresenter.class
)
public interface RssPresenterComponent {
    void inject(MainActivity activity);
}
