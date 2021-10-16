package seedu.address.model.tuition;

import static java.util.Objects.requireNonNull;

/**
 * Represents the name of the tuition class
 */
public class ClassName {

    public final String name;

    /**
     * Constructor for Class Name.
     *
     * @param name
     */
    public ClassName(String name) {
        requireNonNull(name);
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ClassLimit // instanceof handles nulls
                && name == ((ClassName) other).name); // state check
    }

    public String getName() {
        return name;
    }
}