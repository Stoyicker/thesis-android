package org.thesis.android.ui.card.tag;

public interface ITagCard {

    public String getName();

    public interface ITagChangedListener {
        public void onTagCreated(ITagCard tag);

        public void onTagAdded(ITagCard tag);

        public void onTagRemoved(ITagCard tag);

        public void onTagCreationCancelled(ITagCard tag);
    }
}
