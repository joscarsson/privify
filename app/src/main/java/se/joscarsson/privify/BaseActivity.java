package se.joscarsson.privify;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public abstract class BaseActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener, DrawerLayout.DrawerListener {
    protected ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private int selectedMenuId;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        this.drawerLayout = findViewById(R.id.drawer_layout);
        this.drawerLayout.addDrawerListener(this);

        this.drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        this.drawerToggle.setDrawerIndicatorEnabled(true);

        this.navigationView = findViewById(R.id.navigation_view);
        this.navigationView.getMenu().getItem(0).setOnMenuItemClickListener(this);
        this.navigationView.getMenu().getItem(1).setOnMenuItemClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);
    }

    protected abstract int getMenuItemId();

    @Override
    protected void onResume() {
        super.onResume();
        this.selectedMenuId = getMenuItemId();
        this.navigationView.setCheckedItem(this.selectedMenuId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return this.drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        this.selectedMenuId = item.getItemId();
        this.drawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {
        if (this.selectedMenuId == getMenuItemId()) return;

        if (this.selectedMenuId == R.id.settings_menu_item) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}
