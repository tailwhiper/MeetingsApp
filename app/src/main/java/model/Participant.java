package model;

import java.io.Serializable;

public class Participant implements Serializable {
    private String Name;
    private String Job;

    public Participant(String name, String job) {
        Name = name;
        Job = job;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getJob() {
        return Job;
    }

    public void setJob(String job) {
        Job = job;
    }

    @Override
    public boolean equals(Object object) {
        boolean same = false;
        if ((object != null) & (object instanceof Participant)) {
            same = this.Name.equals(((Participant) object).Name) && this.Job.equals(((Participant) object).Job);
        }
        return same;
    }
}
