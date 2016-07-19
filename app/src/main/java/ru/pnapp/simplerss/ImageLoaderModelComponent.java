package ru.pnapp.simplerss;


import dagger.Component;

@Component (
        modules = SimpleImageLoader.class
)
public interface ImageLoaderModelComponent {
    ImageLoaderModel imageLoaderModel();
}
