# The app day ends at 05:00

An outing that ends at 03:00 and a daily check made at 02:00 belong to the evening before, so every record is keyed by an app day running from 05:00 to 05:00 instead of the calendar date. The cutoff lives in one place in `core/datetime/`; changing it later would reassign stored records to different days.
