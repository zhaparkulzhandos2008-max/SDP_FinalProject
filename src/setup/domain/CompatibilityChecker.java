package setup.domain;

import java.util.ArrayList;
import java.util.List;

/** Business rules: socket match and power-supply headroom. */
public final class CompatibilityChecker {
    public static final int HEADROOM_WATTS = 100;

    public List<String> check(PcBuild b) {
        List<String> issues = new ArrayList<>();
        Component cpu = b.part(ComponentType.CPU), mb = b.part(ComponentType.MOTHERBOARD);
        Component gpu = b.part(ComponentType.GPU), psu = b.part(ComponentType.PSU);

        if (cpu != null && mb != null && !cpu.socket().equals(mb.socket()))
            issues.add("CPU socket " + cpu.socket() + " does not fit motherboard socket " + mb.socket());

        if (psu != null) {
            int need = (cpu == null ? 0 : cpu.power()) + (gpu == null ? 0 : gpu.power()) + HEADROOM_WATTS;
            if (need > psu.power())
                issues.add("Power supply " + psu.power() + "W is too weak: at least " + need + "W needed");
        }
        return issues;
    }
}
