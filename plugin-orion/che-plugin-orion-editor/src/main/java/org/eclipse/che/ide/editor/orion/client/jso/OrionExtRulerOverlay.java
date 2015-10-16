/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A Ruler is a graphical element that is placed either on the left or on the right side of the view. It can be used to provide the view with per
 * line decoration such as line numbering, bookmarks, breakpoints, folding disclosures, etc.
 * There are two types of rulers: page and document. A page ruler only shows the content for the lines that are visible, while a document ruler
 * always shows the whole content.
 */
public class OrionExtRulerOverlay extends JavaScriptObject {

    /**
     * Instantiates a new Orion ext ruler overlay.
     */
    protected OrionExtRulerOverlay() {
    }

    /**
     * Add annotation type to the receiver.
     * Only annotations of the specified types will be shown by the receiver.
     * If the priority is not specified, the annotation type will be added to the end of the receiver's list (lowest priority).
     *
     * @param type
     *         the annotation type to be shown
     * @param priority
     *         the priority for the annotation type
     */
    public final native void addAnnotationType(String type, int priority) /*-{
        this.addAnnotationType(type, priority);
    }-*/;

    /**
     * Returns the annotation model.
     *
     * @return the ruler annotation model
     */
    public final native OrionAnnotationModelOverlay getAnnotationModel() /*-{
        return this.getAnnotationModel();
    }-*/;

    /**
     * Returns the annotations for a given line range merging multiple annotations when necessary.
     * This method is called by the text view when the ruler is redrawn.
     *
     * @param startLine
     *         the start line index
     * @param endLine
     *         the end line index
     * @return the annotations for the line range. The array might be sparse.
     */
    public final native OrionAnnotationOverlay[] getAnnotations(int startLine, int endLine) /*-{
        return this.getAnnotations(startLine, endLine);
    }-*/;

    /**
     * Returns an array of annotations in the specified annotation model for the given range of text sorted by type.
     * Defined in: </jobs/genie.orion/orion-client-stable/workspace/bundles/org.eclipse.orion.client.editor/web/orion/editor/annotations.js>.
     *
     * @param annotationModel
     *         the annotation model
     * @param start
     *         the start offset of the range
     * @param end
     *         he end offset of the range
     * @return an annotation array
     */
    public final native OrionAnnotationOverlay[] getAnnotationsByType(OrionAnnotationModelOverlay annotationModel, int start, int end) /*-{
        return this.getAnnotationsByType(annotationModel, start, end);
    }-*/;

    /**
     * Gets the annotation type priority. The priority is determined by the order the annotation type is added to the receiver.
     * Annotation types added first have higher priority. Returns 0 if the annotation type is not added.
     * Defined in: </jobs/genie.orion/orion-client-stable/workspace/bundles/org.eclipse.orion.client.editor/web/orion/editor/annotations.js>.
     *
     * @param type
     *         the annotation type
     * @return the annotation type priority
     */
    public final native int getAnnotationTypePriority(JavaScriptObject type) /*-{
        return this.getAnnotationTypePriority(type);
    }-*/;

    /**
     * Returns the widest annotation which determines the width of the ruler.
     * If the ruler does not have a fixed width it should provide the widest annotation to avoid the ruler from changing size as the view scrolls.
     * This method is called by the text view when the ruler is redrawn.
     *
     * @return the widest annotation
     */
    public final native OrionAnnotationOverlay getWidestAnnotation() /*-{
        return this.getWidestAnnotation();
    }-*/;

    /**
     * Returns whether the receiver shows annotations of the specified type.
     * Defined in: </jobs/genie.orion/orion-client-stable/workspace/bundles/org.eclipse.orion.client.editor/web/orion/editor/annotations.js>.
     *
     * @param type
     *         the annotation type
     * @return whether the specified annotation type is shown
     */
    public final native boolean isAnnotationTypeVisible(String type) /*-{
        return this.isAnnotationTypeVisible(type);
    }-*/;

    /**
     * Removes an annotation type from the receiver.
     * Defined in: </jobs/genie.orion/orion-client-stable/workspace/bundles/org.eclipse.orion.client.editor/web/orion/editor/annotations.js>.
     *
     * @param type
     *         the annotation type to be removed
     */
    public final native void removeAnnotationType(String type) /*-{
        this.removeAnnotationType(type);
    }-*/;

    /**
     * Sets the annotation model for the ruler.
     *
     * @param annotationModel
     *         the annotation model
     */
    public final native void setAnnotationModel(OrionAnnotationModelOverlay annotationModel) /*-{
        this.setAnnotationModel(annotationModel);
    }-*/;

    /**
     * Sets the annotation that is displayed when a given line contains multiple annotations. This annotation is used when there are different types
     * of annotations in a given line.
     *
     * @param annotation
     *         the annotation for lines with multiple annotations
     */
    public final native void setMultiAnnotation(OrionAnnotationOverlay annotation) /*-{
        this.setMultiAnnotation(annotation);
    }-*/;

    /**
     * Sets the annotation that overlays a line with multiple annotations. This annotation is displayed on top of the computed annotation for a given
     * line when there are multiple annotations of the same type in the line. It is also used when the multiple annotation is not set.
     *
     * @param annotation
     *        the annotation overlay for lines with multiple annotations
     */
    public final native void setMultiAnnotationOverlay(OrionAnnotationOverlay annotation) /*-{
        this.setMultiAnnotationOverlay(annotation);
    }-*/;

    /**
     * Add event listener.
     *
     * @param <T>
     *         the type parameter
     * @param eventType
     *         the event type
     * @param handler
     *         the handler
     * @param useCapture
     *         the use capture
     */
    public final native <T extends OrionEventOverlay> void addEventListener(String eventType, EventHandler<T> handler, boolean useCapture) /*-{
        var func = function (param) {
            handler.@org.eclipse.che.ide.editor.orion.client.jso.OrionExtRulerOverlay.EventHandler::onEvent(*)(param);
        };
        this.addEventListener(eventType, func, useCapture);
    }-*/;

    /**
     * Constructs a new key stroke with the given key code, modifiers and event type.
     *
     * @param annotationModel
     *         the annotation model for the ruler
     * @param rulerLocation
     *         the location for the ruler, either left or right
     * @param rulerOverview
     *         the overview for the ruler, either page or document
     * @param rulerStyle
     *         the style for the ruler.
     * @param orionEditorRulerModule
     *         the orion editor ruler module
     * @return the orion ext ruler overlay
     */
    public static final native OrionExtRulerOverlay create(JavaScriptObject annotationModel,
                                                           String rulerLocation,
                                                           String rulerOverview,
                                                           JavaScriptObject rulerStyle,
                                                           JavaScriptObject orionEditorRulerModule) /*-{
        return new orionEditorRulerModule.ExtRuler(annotationModel, rulerLocation, rulerOverview, rulerStyle);
    }-*/;

    /**
     * The interface Event handler.
     *
     * @param <T>
     *         the type parameter
     */
    public interface EventHandler<T extends OrionEventOverlay> {
        /**
         * On event.
         *
         * @param parameter
         *         the parameter
         */
        void onEvent(T parameter);
    }
}
