package org.thesis.android.ui.util;

import android.annotation.SuppressLint;
import android.content.Context;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;

@SuppressLint("ViewConstructor")//Never ever instantiated through XML anyway
public class TagView extends CardView {

    public TagView(Context context, String tagName) {
        super(context);

        final Card c = new Card(context);
        c.setTitle(tagName);
        this.setCard(c);
    }

}
