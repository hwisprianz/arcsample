package com.z.arc.recyclerview.viewholder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.InvocationTargetException;

/**
 * 使用ViewBinding 的ViewHolder
 * <p>
 * Created by Blate on 2023/6/14
 */
@SuppressWarnings("unused")
public class ViewHolderWithBinding<T extends ViewBinding> extends RecyclerView.ViewHolder {

    /**
     * 用于创建ViewBinding的接口
     * <p>
     * 不需要实现这个接口. 使用方法引用, ex:
     * <pre class="androidx.recyclerview.widget.RecyclerView.Adapter">
     * {@code @NonNull}
     * {@code @Override}
     * public ViewHolderWithBinding<ViewBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
     *      return ViewHolderWithBinding.create(parent, ViewBinding::inflate);
     * }
     * </pre>
     *
     * @param <VB> ViewBinding
     * @see #create(ViewGroup, Class)
     */
    public interface ViewInflater<VB> {

        VB inflate(LayoutInflater inflater, ViewGroup parent, boolean attachToParent);

    }

    @NonNull
    public final T viewBinding;

    public ViewHolderWithBinding(@NonNull T viewBinding) {
        super(viewBinding.getRoot());
        this.viewBinding = viewBinding;
    }

    /**
     * 通过反射创建ViewHolderWithBinding
     * <p>
     * 尽量使用{@link ViewHolderWithBinding#create(ViewGroup, ViewInflater)};
     * 同样的方便,不使用反射更安全,更高效^_^
     *
     * @param parent parent
     * @param klass  ViewBinding class
     * @param <VB>   ViewBinding
     * @return ViewHolderWithBinding
     */
    @SuppressWarnings("unchecked")
    public static <VB extends ViewBinding> ViewHolderWithBinding<VB> create(ViewGroup parent, Class<VB> klass) {
        try {
            final VB viewBinding = (VB) klass.getMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class)
                    .invoke(null, LayoutInflater.from(parent.getContext()), parent, false);
            if (viewBinding == null) {
                throw new RuntimeException("ViewBinding inflate failed");
            } else {
                return new ViewHolderWithBinding<>(viewBinding);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("ViewHolderWithBinding#create(ViewGroup, Class) failed. check you codes or use constructor: ViewHolderWithBinding(ViewBinding)", e);
        }
    }

    /**
     * 通过ViewInflater创建ViewHolderWithBinding
     * <p>
     * 不使用反射的版本, 更安全, 更高效^_^
     * <p>
     * ex:
     * <pre class="androidx.recyclerview.widget.RecyclerView.Adapter">
     * {@code @NonNull}
     * {@code @Override}
     * public ViewHolderWithBinding<ViewBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
     *      return ViewHolderWithBinding.create(parent, ViewBinding::inflate);
     * }
     * </pre>
     *
     * @param viewGroup viewGroup
     * @param inflater  inflater
     * @param <VB>      ViewBinding
     * @return ViewHolderWithBinding
     */
    public static <VB extends ViewBinding> ViewHolderWithBinding<VB> create(@NonNull ViewGroup viewGroup, @NonNull ViewInflater<VB> inflater) {
        return new ViewHolderWithBinding<>(inflater.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

}
