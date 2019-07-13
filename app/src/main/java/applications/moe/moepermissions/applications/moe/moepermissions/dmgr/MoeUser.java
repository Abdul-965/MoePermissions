package applications.moe.moepermissions.applications.moe.moepermissions.dmgr;

public class MoeUser {
    private String _uid;
    private String _name;
    private int _role;

    public MoeUser() {
        this._uid = "";
        this._name = "";
        this._role = 0;
    }

    public MoeUser(String _uid, String _name, int _role) {
        this._uid = _uid;
        this._name = _name;
        this._role = _role;
    }

    public String get_uid() {
        return _uid;
    }

    public void set_uid(String _uid) {
        this._uid = _uid;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public int get_role() {
        return _role;
    }

    public void set_role(int _role) {
        this._role = _role;
    }
}
