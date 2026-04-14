import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../features/effects/effects_cubit.dart';
import '../features/effects/effects_screen.dart';
import '../features/pedalboard/pedalboard_screen.dart';
import '../features/tuner/tuner_screen.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import 'home_tab.dart';

enum ShellTab { home, effects, pedals, tuner }

class AppShell extends StatefulWidget {
  const AppShell({super.key});

  @override
  State<AppShell> createState() => _AppShellState();
}

class _AppShellState extends State<AppShell> {
  ShellTab _current = ShellTab.home;

  void _switchTab(ShellTab tab) => setState(() => _current = tab);

  @override
  Widget build(BuildContext context) {
    return BlocProvider<EffectsCubit>(
      create: (_) => EffectsCubit(),
      child: Scaffold(
        backgroundColor: AppColors.bg,
        body: IndexedStack(
          index: _current.index,
          children: [
            HomeTab(onSwitchTab: _switchTab),
            const EffectsScreen(),
            const PedalboardScreen(),
            const TunerScreen(),
          ],
        ),
        bottomNavigationBar: Container(
          decoration: const BoxDecoration(
            color: AppColors.navBar,
            border: Border(
              top: BorderSide(color: Color(0xFF151B26), width: 1),
            ),
          ),
          child: SafeArea(
            top: false,
            child: SizedBox(
              height: 64,
              child: Row(
                children: [
                  _NavItem(
                    icon: Icons.home_rounded,
                    label: 'Home',
                    selected: _current == ShellTab.home,
                    onTap: () => _switchTab(ShellTab.home),
                  ),
                  _NavItem(
                    icon: Icons.graphic_eq_rounded,
                    label: 'Effects',
                    selected: _current == ShellTab.effects,
                    onTap: () => _switchTab(ShellTab.effects),
                  ),
                  _NavItem(
                    icon: Icons.dashboard_customize_rounded,
                    label: 'Pedals',
                    selected: _current == ShellTab.pedals,
                    onTap: () => _switchTab(ShellTab.pedals),
                  ),
                  _NavItem(
                    icon: Icons.music_note_rounded,
                    label: 'Tuner',
                    selected: _current == ShellTab.tuner,
                    onTap: () => _switchTab(ShellTab.tuner),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _NavItem extends StatelessWidget {
  const _NavItem({
    required this.icon,
    required this.label,
    required this.selected,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final color = selected ? AppColors.primary : AppColors.textTertiary;
    return Expanded(
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onTap,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, color: color, size: 22),
              const SizedBox(height: 4),
              Text(
                label,
                style: AppTypography.tabLabel.copyWith(color: color),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
