package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LIMIT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIMESLOT;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.tuition.ClassLimit;
import seedu.address.model.tuition.ClassName;
import seedu.address.model.tuition.Timeslot;
import seedu.address.model.tuition.TuitionClass;

public class EditClassCommand extends Command {
    public static final String COMMAND_WORD = "editclass";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits details of the class identified by the index numbers used in the Classes list.\n"
            + "Parameters: CLASS_INDEX "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_TIMESLOT + "TIMESLOT] "
            + "[" + PREFIX_LIMIT + "LIMIT]\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_NAME + "Physics "
            + PREFIX_TIMESLOT + "Mon 11:00-11:30 "
            + PREFIX_LIMIT + "20";

    public static final String MESSAGE_EDIT_CLASS_SUCCESS = "Edited: %1$s";
    public static final String MESSAGE_NO_CLASS_CHANGES = "Class details are already up to date.";
    public static final String MESSAGE_INVALID_CLASS_LIMIT = "Minimum limit of class is %1$s.";
    public static final String MESSAGE_INVALID_CLASS_SLOT = "This timeslot is already taken. Check timetable!";

    private final Index index;
    private final EditClassDescriptor editClassDescriptor;

    /**
     *
     * @param index index of the class in the filtered classes list to edit.
     * @param editClass details of the class to edit.
     */
    public EditClassCommand(Index index, EditClassDescriptor editClass) {
        requireNonNull(index);
        requireNonNull(editClass);
        this.index = index;
        this.editClassDescriptor = editClass;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<TuitionClass> lastShownList = model.getFilteredTuitionList();
        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX);
        }
        TuitionClass classToEdit = lastShownList.get(index.getZeroBased());
        TuitionClass editedClass = createEditedClass(classToEdit, editClassDescriptor);
        if (classToEdit.sameClassDetails(editedClass)) {
            throw new CommandException(String.format(MESSAGE_NO_CLASS_CHANGES));
        }
        //check limit minimum limit of class = current number of students
        if (editedClass.getLimit().getLimit() < classToEdit.getStudentCount()) {
            throw new CommandException(String.format(MESSAGE_INVALID_CLASS_LIMIT, classToEdit.getStudentCount()));
        }
        //check if updated timeslot is taken
        if (!editedClass.getTimeslot().equals(classToEdit.getTimeslot())) {
            if (editedClass.getTimeslot().checkTimetableConflicts(lastShownList)) {
                throw new CommandException(MESSAGE_INVALID_CLASS_SLOT);
            }
        }
        model.setTuition(classToEdit, editedClass);
        return new CommandResult(String.format(MESSAGE_EDIT_CLASS_SUCCESS, editedClass));
    }


    private static TuitionClass createEditedClass(TuitionClass classToEdit, EditClassDescriptor editClassDescriptor) {
        assert classToEdit != null;

        ClassName updatedName = editClassDescriptor.getName().orElse(classToEdit.getName());
        ClassLimit updatedLimit = editClassDescriptor.getLimit().orElse(classToEdit.getLimit());
        Timeslot updatedSlot = editClassDescriptor.getTimeslot().orElse(classToEdit.getTimeslot());

        return new TuitionClass(updatedName, updatedLimit, updatedSlot,
                classToEdit.getStudentList(), classToEdit.getRemark());
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditClassDescriptor {
        private ClassName name;
        private ClassLimit limit;
        private Timeslot timeslot;

        public EditClassDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditClassDescriptor(EditClassDescriptor toCopy) {
            setClassName(toCopy.name);
            setLimit(toCopy.limit);
            setTimeslot(toCopy.timeslot);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, limit, timeslot);
        }

        public void setClassName(ClassName name) {
            this.name = name;
        }

        public Optional<ClassName> getName() {
            return Optional.ofNullable(name);
        }

        public void setLimit(ClassLimit limit) {
            this.limit = limit;
        }

        public Optional<ClassLimit> getLimit() {
            return Optional.ofNullable(limit);
        }

        public void setTimeslot(Timeslot timeslot) {
            this.timeslot = timeslot;
        }

        public Optional<Timeslot> getTimeslot() {
            return Optional.ofNullable(timeslot);
        }

        @Override
        public boolean equals(Object other) {
            // short circuit if same object
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditCommand.EditPersonDescriptor)) {
                return false;
            }

            // state check
            EditClassDescriptor e = (EditClassDescriptor) other;

            return getName().equals(e.getName())
                    && getLimit().equals(e.getLimit())
                    && getTimeslot().equals(e.getTimeslot());
        }
    }
}


